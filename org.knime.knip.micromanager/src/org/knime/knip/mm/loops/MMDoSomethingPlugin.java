package org.knime.knip.mm.loops;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ShortProcessor;
import net.imglib2.type.numeric.RealType;

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
	private double z = 0;

	@Parameter(type = ItemIO.OUTPUT, label = "Info")
	private String info = "";

	@Override
	public void run() {

		final MMCore core = MMGateway.getInstance().getMMCore();

		info = core.getVersionInfo();
		core.setExposure(10);
		core.waitForSystem();

		core.setROI(0, 0, 200, 200);
		core.startSequenceAcquisition(10000, 0, false);
		int i = 0;
		while (core.getRemainingImageCount() > 0
				|| core.isSequenceRunning(core.getCameraDevice())) {
			if (core.getRemainingImageCount() > 0) {
				final Object img = core.popNextImage();
				// do whatever you want with the image
				// save pre-frame to TIF
				final long width = core.getImageWidth();
				final long height = core.getImageHeight();
				final long byteDepth = core.getBytesPerPixel();

				final ShortProcessor ip = new ShortProcessor((int) width,
						(int) height);
				ip.setPixels(img);

				i = i + 1;
				final ImagePlus imp = new ImagePlus(prePath + i + ".tif", ip);
				final FileSaver fs = new FileSaver(imp);
				fs.saveAsTiff(prePath + i + ".tif");
			}
		}

		core.stopSequenceAcquisition();
	}
}
