package org.knime.knip.mm.loops;

import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;

import org.knime.knip.mm.MMGateway;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(menu = {@Menu(label = "Micro-Manager"),
				@Menu(label = "Snap Image with Micro-Manager")},
		description = "Acquires a single frame using Micro-Manager",
		headless = true,
		type = Command.class)//,
		//iconPath = "laser.png")
public class MMSnapImage<T extends RealType<T>> implements Command {

	// TODO: replace this with a real port
	@Parameter(type = ItemIO.INPUT)
	protected String dummy;

	@Parameter(type = ItemIO.OUTPUT, label = "Image")
	private ImgPlus<T> outImg;

	@Override
	public void run() {
		final MMGateway mm = MMGateway.getInstance();

		mm.snapImage();
		final long width = mm.getImageWidth();
		final long height = mm.getImageHeight();
		final long[] dims = new long[] { width, height };
		final Object pixels = mm.getImage();
		final Img<T> img;
		// TODO: support multi-channel images
		if (pixels instanceof byte[]) {
			img = (Img<T>) ArrayImgs.bytes((byte[]) pixels, dims);
		}
		else if (pixels instanceof short[]) {
			img = (Img<T>) ArrayImgs.bytes((byte[]) pixels, dims);
		}
		else {
			throw new RuntimeException("Unsupported pixel type!");
		}
		// TODO: provide all available metadata
		outImg = new ImgPlus<T>(img);
	}
}