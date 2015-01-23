package org.knime.knip.mm.loops;

import java.io.File;

import org.knime.knip.mm.CMMCoreWrapper;

public class MMGateway {

	private static MMGateway m_gateway;

	// replace Object with MMCore
	// This guy loads MMCore globally into KNIME...
	// That way, other plugins can register themselves and use Micro-Manager
	private CMMCoreWrapper m_core = null;

	private MMGateway() {
		try {
			final String microManagerDirectory = "C:\\Program Files\\Micro-Manager-1.4\\";
			m_core = new CMMCoreWrapper(new File(microManagerDirectory));
			
			//load existing system configuration file:
			m_core.loadSystemConfiguration("C:\\Program Files\\Micro-Manager-1.4\\MMConfig_OrcaFlash4.cfg");
			
//			m_core.loadDevice("Camera", "HamamatsuHam", "HamamatsuHam_DCAM");
//
//			// Serial Port COM3 setup:
//			m_core.loadDevice("Port", "SerialManager", "COM3");
//			m_core.setProperty("Port", "AnswerTimeout", 2500);
//			m_core.setProperty("Port", "BaudRate", 500000);
//			m_core.setProperty("Port", "DelayBetweenCharsMs", 0.0);
//			m_core.setProperty("Port", "Handshaking", "Off");
//			m_core.setProperty("Port", "StopBits", "1");
//			m_core.setProperty("Port", "Parity", "None");
//			m_core.setProperty("Port", "Verbose", "1");
//			
//			// Omicron 488nm setup, COM3:
//			m_core.loadDevice("Omicron-488nm", "Omicron", "Omicron");
////			m_core.setProperty("Omicron-488nm", "Port", "Port1");
////			
//			m_core.initializeDevice("Port");
//			m_core.initializeDevice("Omicron-488nm")
			
			//initialize devices:
			m_core.initializeAllDevices();
			m_core.setShutterOpen(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static MMGateway getInstance() {
		if (m_gateway == null)
			m_gateway = new MMGateway();

		return m_gateway;
	}

	public CMMCoreWrapper getMMCore() {
		return m_core;
	}
}
