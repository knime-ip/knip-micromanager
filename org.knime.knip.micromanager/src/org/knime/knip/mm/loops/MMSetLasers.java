package org.knime.knip.mm.loops;

import net.imglib2.type.numeric.RealType;

import org.knime.knip.mm.CMMCoreWrapper;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.NumberWidget;

@Plugin(menu = {@Menu(label = "DeveloperPlugins"),
				@Menu(label = "SetLasers")}, 
		description = "Turn Lasers on/off, set intensities", 
		headless = true, 
		type = Command.class)//,
		//iconPath = "laser.png")
public class MMSetLasers<T extends RealType<T>> implements Command {

	@Parameter(label = "Laser1: 405 nm",
			style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE,
			choices = {"Off", "On"})
    private String choice405;
	
	@Parameter(type = ItemIO.BOTH, 
			label = "Power (max: 60 mW):", 
			style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "60")
    private double P405 = 0.0;
	
	@Parameter(label = "Laser2: 488 nm", 
			style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE,
			choices = {"Off", "On"})
    private String choice488;
	
	@Parameter(type = ItemIO.BOTH, 
			label = "Power (max: 200 mW):", 
			style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "200")
    private double P488 = 0.0;
	
	@Parameter(label = "Laser3: 515 nm",
			style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE,
			choices = {"Off", "On"})
    private String choice515;
	
	@Parameter(type = ItemIO.BOTH, 
			label = "Power (max: 150 mW):", 
			style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "150")
    private double P515 = 0.0;
	
	@Parameter(label = "Laser4: 647 nm", 
			style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE,
			choices = {"Off", "On"})
    private String choice647;
	
	@Parameter(type = ItemIO.BOTH, 
			label = "Power (max: 140 mW):", 
			style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "140")
    private double P647 = 0.0;
	
	@Override
	public void run() {

		final CMMCoreWrapper core = MMGateway.getInstance().getMMCore();

		try {
			core.waitForSystem();
			//405nm:
			core.setProperty("Omicron-405nm", "Laser Operation Select", choice405);
			core.setProperty("Omicron-405nm", "Laser Power Set-point Select [mW]", P405);
			//488nm:
			core.setProperty("Omicron-488nm", "Laser Operation Select", choice488);
			core.setProperty("Omicron-488nm", "Laser Power Set-point Select [mW]", P488);
			//515nm:
			core.setProperty("Cobolt-515nm", "Laser", choice515);
			core.setProperty("Cobolt-515nm", "PowerSetpoint", P515);
			//647nm:
			core.setProperty("Omicron-647nm", "Laser Operation Select", choice647);
			core.setProperty("Omicron-647nm", "Laser Power Set-point Select [mW]", P647);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}