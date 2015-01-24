package org.knime.knip.mm;

import static org.knime.knip.mm.Reflection.construct;
import static org.knime.knip.mm.Reflection.invoke;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * Wraps a CMMCore class of a given Micro-Manager installation via reflection.
 * 
 * @author Johannes Schindelin
 */
public class MMGateway {

	private static final String CMMCORE_CLASS_NAME = "mmcorej.CMMCore";
	private final ClassLoader loader;
	private final Object core;
	private final boolean suggestMissingMethods = true;

	private static MMGateway instance;

	/**
	 * Get the Micro-Manager singleton
	 * 
	 * Micro-Manager cannot be instantiated multiple times (it would try to load
	 * the native library multiple times). So there can only be one instance.
	 * 
	 * @return the instance of the Micro-Manager gateway
	 */
	public synchronized static MMGateway getInstance() {
		if (instance == null) {
			throw new RuntimeException("Need to configure Micro-Manager first!");
		}
		return instance;
	}

	/**
	 * Initialize the Micro-Manager singleton
	 * 
	 * @param microManagerDirectory the top-level Micro-Manager directory
	 * @return the singleton
	 */
	public synchronized static MMGateway createInstance(final File microManagerDirectory) {
		if (instance != null) {
			throw new RuntimeException("Micro-Manager can only be configured once!");
		}
		try {
			return instance = new MMGateway(microManagerDirectory);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Discover the top-level Micro-Manager directory.
	 * 
	 * This method looks in the default places to find Micro-Manager.
	 * 
	 * @return the top-level directory, or null if no Micro-Manager was found
	 */
	public static File discoverMicroManager() {
		File directory = new File("C:\\Program Files\\Micro-Manager-1.4");
		if (directory.isDirectory()) return directory;
		directory = new File("/Applications/Micro-Manager-1.4.app");
		if (directory.isDirectory()) return directory;
		directory = new File(System.getProperty("user.home") + "/Micro-Manager-1.4");
		if (directory.isDirectory()) return directory;
		directory = new File(System.getProperty("user.home") + "/Desktop/Fiji.app");
		if (directory.isDirectory()) return directory;
		return null;
	}

	/**
	 * Instantiates a CMMCore wrapper.
	 * 
	 * @param microManagerDirectory the top-level directory of an existing Micro-Manager installation
	 */
	private MMGateway(final File microManagerDirectory) throws IOException {
		final File mmcorej = new File(microManagerDirectory, "plugins/Micro-Manager/MMCoreJ.jar");
		if (!mmcorej.exists()) {
			throw new IOException("Could not find Micro-Manager at " + mmcorej);
		}
		loader = new URLClassLoader(new URL[] { mmcorej.toURI().toURL() });
		core = construct(loader, CMMCORE_CLASS_NAME);
		verifySignatures();
		if (suggestMissingMethods) {
			suggestMissingMethods();
		}
	}

	private void verifySignatures() {
		final StringBuilder builder = new StringBuilder();
		final Class<?> coreClass = core.getClass();
		for (final Method method : getClass().getMethods()) {
			if ((method.getModifiers() & Modifier.STATIC) != 0) {
				continue;
			}
			try {
				final Method found = coreClass.getMethod(method.getName(), method.getParameterTypes());
				if (!method.getReturnType().isAssignableFrom(found.getReturnType())) {
					builder.append(method.getName()).append("(").append(Arrays.toString(method.getParameterTypes()))
						.append(") has incorrect return type: ").append(method.getReturnType().getName())
						.append(" != ").append(found.getReturnType()).append("\n");
				}
			} catch (NoSuchMethodException e) {
				builder.append(method.getName()).append("(").append(method.getParameterTypes())
				.append(") not found in class ").append(coreClass.getName());
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		if (builder.length() > 0) {
			throw new RuntimeException("Incompatible CMMCore class:\n" + builder);
		}
	}

	private void suggestMissingMethods() {
		final Class<?> coreClass = core.getClass();
		final Class<?> thisClass = getClass();
		for (final Method method : coreClass.getMethods()) {
			try {
				thisClass.getMethod(method.getName(), method.getParameterTypes());
				continue;
			} catch (NoSuchMethodException e) {
				// fall through
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
			final Class<?>[] parameterTypes = method.getParameterTypes();
			final String[] parameterNames = new String[parameterTypes.length];
			for (int i = 0; i < parameterNames.length; i++) {
				if (parameterTypes[i].getName().startsWith("mmcorej.")) {
					parameterTypes[i] = Object.class;
				}
				parameterNames[i] = "arg" + i;
			}

			final StringBuilder builder = new StringBuilder();
			String returnType = method.getReturnType().getName();
			if (returnType.startsWith("mmcorej.")) {
				returnType = "Object";
			}
			else {
				returnType = returnType.substring(returnType.lastIndexOf('.') + 1);
			}
			builder.append("\tpublic ").append(returnType).append(" ").append(method.getName()).append("(");
			for (int i = 0; i < parameterTypes.length; i++) {
				if (i > 0) {
					builder.append(", ");
				}
				String type = parameterTypes[i].getName();
				type = type.substring(type.lastIndexOf('.') + 1);
				builder.append("final ").append(type).append(" ").append(parameterNames[i]);
			}
			builder.append(") {\n\t\t");
			if (method.getReturnType() != Void.TYPE) {
				builder.append("return ");
			}
			builder.append("invoke(core, \"").append(method.getName()).append("\"");
			for (int i = 0; i < parameterTypes.length; i++) {
				builder.append(", ").append(parameterNames[i]);
			}
			builder.append(");\n\t}\n");
			System.err.println(builder.toString());
		}
	}

	public Iterable<String> getDeviceAdapterNames() {
		return invoke(core, "getDeviceAdapterNames");
	}

	public void loadSystemConfiguration(final String fileName) {
		invoke(core, "loadSystemConfiguration", fileName);
	}

	public void initializeAllDevices() {
		invoke(core, "initializeAllDevices");
	}

	public void setShutterOpen(final boolean state) {
		invoke(core, "setShutterOpen", state);
	}

	public void loadDevice(final String label, final String library, final String adapterName) {
		invoke(core, "loadDevice", label, library, adapterName);
	}

	public void snapImage() {
		invoke(core, "snapImage");
	}

	public Object getImage() {
		return invoke(core, "getImage");
	}

	public Object getImage(final long numChannel) {
		return invoke(core, "getImage", numChannel);
	}

	public long getImageWidth() {
		return invoke(core, "getImageWidth");
	}

	public long getImageHeight() {
		return invoke(core, "getImageHeight");
	}

	public long getBytesPerPixel() {
		return invoke(core, "getBytesPerPixel");
	}

	public long getImageBitDepth() {
		return invoke(core, "getImageBitDepth");
	}

	public long getNumberOfComponents() {
		return invoke(core, "getNumberOfComponents");
	}

	public void setProperty(final String label, final String propName, final int propValue) {
		invoke(core, "setProperty", label, propName, propValue);
	}

	public void setProperty(final String label, final String propName, final boolean propValue) {
		invoke(core, "setProperty", label, propName, propValue);
	}

	public void setProperty(final String label, final String propName, final String propValue) {
		invoke(core, "setProperty", label, propName, propValue);
	}

	public void setProperty(final String label, final String propName, final float propValue) {
		invoke(core, "setProperty", label, propName, propValue);
	}

	public void setProperty(final String label, final String propName, final double propValue) {
		invoke(core, "setProperty", label, propName, propValue);
	}

	public String getProperty(final String label, final String propName) {
		return invoke(core, "getProperty", label, propName);
	}

	public void setExposure(final String label, final double dExp) {
		invoke(core, "setExposure", label, dExp);
	}

	public void setExposure(final double exp) {
		invoke(core, "setExposure", exp);
	}

	public double getExposure() {
		return invoke(core, "getExposure");
	}

	public String getCameraDevice() {
		return invoke(core, "getCameraDevice");
	}

	public String getShutterDevice() {
		return invoke(core, "getShutterDevice");
	}

	public String getFocusDevice() {
		return invoke(core, "getFocusDevice");
	}

	public String getXYStageDevice() {
		return invoke(core, "getXYStageDevice");
	}

	public String getAutoFocusDevice() {
		return invoke(core, "getAutoFocusDevice");
	}

	public String getImageProcessorDevice() {
		return invoke(core, "getImageProcessorDevice");
	}

	public String getSLMDevice() {
		return invoke(core, "getSLMDevice");
	}

	public String getGalvoDevice() {
		return invoke(core, "getGalvoDevice");
	}

	public int getRemainingImageCount() {
		return invoke(core, "getRemainingImageCount");
	}

	public int getBufferTotalCapacity() {
		return invoke(core, "getBufferTotalCapacity");
	}

	public int getBufferFreeCapacity() {
		return invoke(core, "getBufferFreeCapacity");
	}

	public boolean isBufferOverflowed() {
		return invoke(core, "isBufferOverflowed");
	}

	public Object popNextImage() {
		return invoke(core, "popNextImage");
	}

	public void setROI(final int x, final int y, final int xSize, final int ySize) {
		invoke(core, "setROI", x, y, xSize, ySize);
	}

	public void startSequenceAcquisition(final String cameraLabel, final int numImages, final double intervalMs, final boolean stopOnOverflow) {
		invoke(core, "startSequenceAcquisition", cameraLabel, numImages, intervalMs, stopOnOverflow);
	}

	public void startSequenceAcquisition(final int numImages, final double intervalMs, final boolean stopOnOverflow) {
		invoke(core, "startSequenceAcquisition", numImages, intervalMs, stopOnOverflow);
	}

	public void prepareSequenceAcquisition(final String cameraLabel) {
		invoke(core, "prepareSequenceAcquisition", cameraLabel);
	}

	public void startContinuousSequenceAcquisition(final double intervalMs) {
		invoke(core, "startContinuousSequenceAcquisition", intervalMs);
	}

	public void stopSequenceAcquisition() {
		invoke(core, "stopSequenceAcquisition");
	}

	public void stopSequenceAcquisition(final String stopLabel) {
		invoke(core, "stopSequenceAcquisition", stopLabel);
	}

	public boolean isSequenceRunning(final String arg0) {
		return invoke(core, "isSequenceRunning", arg0);
	}

	public boolean isSequenceRunning() {
		return invoke(core, "isSequenceRunning");
	}

	public boolean deviceBusy(final String deviceName) {
		return invoke(core, "deviceBusy", deviceName);
	}

	public void waitForDevice(final String deviceName) {
		invoke(core, "waitForDevice", deviceName);
	}

	public void waitForConfig(final String group, final String configName) {
		invoke(core, "waitForConfig", group, configName);
	}

	public boolean systemBusy() {
		return invoke(core, "systemBusy");
	}

	public void waitForSystem() {
		invoke(core, "waitForSystem");
	}

	public void waitForImageSynchro() {
		invoke(core, "waitForImageSynchro");
	}

	public static void main(final String... args) throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
		final File microManagerDir = discoverMicroManager();
		final MMGateway wrapper = new MMGateway(microManagerDir);

		boolean orcaTest = false;
		if (orcaTest) {
			wrapper.loadSystemConfiguration(new File(microManagerDir, "MMConfig_OrcaFlash4.cfg").getAbsolutePath());
			wrapper.initializeAllDevices();
			wrapper.setShutterOpen(true);
			return;
		}

		final Iterable<String> deviceAdapters = wrapper.getDeviceAdapterNames();
		for (final String name : deviceAdapters) {
			System.err.println("Adapter: " + name);
		}
		wrapper.loadDevice("Camera", "DemoCamera", "DCam");
		wrapper.initializeAllDevices();
		wrapper.snapImage();
		final byte[] pixels = (byte[]) wrapper.getImage();
		System.err.println("Got " + pixels.length + " pixels (" + wrapper.getImageWidth() + "x" + wrapper.getImageHeight() + ")");
	}

}
