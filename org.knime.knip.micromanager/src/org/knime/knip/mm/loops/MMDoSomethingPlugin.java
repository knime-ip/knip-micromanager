package org.knime.knip.mm.loops;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.knime.knip.mm.CMMCoreWrapper;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(menu = { @Menu(label = "DeveloperPlugins"),
		@Menu(label = "My MM Control Unit") }, description = "Very simple example", headless = true, type = Command.class)
public class MMDoSomethingPlugin<T extends RealType<T>> implements Command {

	@Parameter(type = ItemIO.INPUT, label = "Path")
	private String prePath = "C:\\Users\\screening\\Desktop\\images\\image";;

	@Parameter(type = ItemIO.INPUT, label = "X")
	private double x = 50;

	@Parameter(type = ItemIO.INPUT, label = "Y")
	private double y = 50;

	@Parameter(type = ItemIO.INPUT, label = "Z")
	private double z = 50;

	@Parameter(type = ItemIO.INPUT, label = "Frames")
	private int frames = 100;

	@Parameter(type = ItemIO.INPUT, label = "X-Resolution")
	private int xRes = 200;

	@Parameter(type = ItemIO.INPUT, label = "Y-Resolution")
	private int yRes = 200;

	@Parameter(type = ItemIO.OUTPUT, label = "Image")
	private ImgPlus<ShortType> outImg;
	
	@Parameter(type = ItemIO.INPUT, label = "Laser Power")
	private double lp = 50;

	@Override
	public void run() {

		final CMMCoreWrapper core = MMGateway.getInstance().getMMCore();

		try {
			core.setExposure(10);
			core.waitForSystem();
			core.setProperty("Omicron-488nm", "Laser Operation Select", "On");
			core.setProperty("Omicron-488nm", "Laser Power Set-point Select [mW]", 200);
			// na dann mal los : - )
			core.setROI(0, 0, xRes, yRes);
			core.startSequenceAcquisition(frames, 0, false);
			int i = 0;

			// create image with some metadata :-)))
			outImg = new ImgPlus<ShortType>(
					ArrayImgs.shorts(xRes, yRes, frames), "Manuels Img",
					new AxisType[] { Axes.X, Axes.Y, Axes.TIME });

			int frame = 0;
			while (core.getRemainingImageCount() > 0
					|| core.isSequenceRunning(core.getCameraDevice()) || false) {
				if (core.getRemainingImageCount() > 0) {
					final short[] img = (short[]) core.popNextImage();
					// do whatever you want with the image
					
					// access plane by plane. Interval
					IntervalView<ShortType> interval = Views.interval(outImg,
							new FinalInterval(new long[] { 0, 0, frame },
									new long[] { xRes-1, yRes-1, frame }));

					// wieder als bild verpacken und schon haben wir ein
					// zweidimensionales ding. sweet oder? ;-)
					Img<ShortType> plane = new ImgView<>(interval, null);

					// cursor als pixelzugriff
					Cursor<ShortType> cursor = plane.cursor();

					int k = 0;
					while (cursor.hasNext()) {
						cursor.fwd();
						cursor.get().set(img[k++]);
					}

					// i = i + 1;
					// final ImagePlus imp = new ImagePlus(prePath + i + ".tif",
					// ip);
					// final FileSaver fs = new FileSaver(imp);
					// fs.saveAsTiff(prePath + i + ".tif");
					 frame++;
				}
			}

			core.stopSequenceAcquisition();
			core.setProperty("Omicron-488nm", "Laser Power Set-point Select [mW]", 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
