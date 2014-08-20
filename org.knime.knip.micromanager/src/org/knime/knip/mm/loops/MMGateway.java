package org.knime.knip.mm.loops;

public class MMGateway {

	private static MMGateway m_gateway;

	// replace Object with MMCore
	private Object m_core = null; 

	private MMGateway() {
		core.loadDevice("Camera", "HamamatsuHam", "HamamatsuHam_DCAM");
		core.initializeAllDevices();
	}

	public static MMGateway getInstance() {
		if (m_gateway == null)
			m_gateway = new MMGateway();

		return m_gateway;
	}

	public Object getMMCore() {
		return m_core;
	}

}
