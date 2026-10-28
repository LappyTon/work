package meteordevelopment.meteorclient.sell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AutosellSetup {
	private AutosellCycle cycle = new AutosellCycle(this);;
	private AutosellRename rename = new AutosellRename();
	private int switches = 0;
    private ArrayList<String> args = new ArrayList<>();
    private ArrayList<String> rawArgs = new ArrayList<>();

	private List<String> logins;
	private List<String> passwords;

	private int loops = 0;

	private int tryes = 0;

	private long lastRestartTime = 0;
	private static final long RESTART_INTERVAL = 2 * 60 * 60 * 1000; // 2 години

	private boolean keyComboPreviouslyPressed = false;
	boolean renamerEnabled = false;


	public void startCycle() {
		if (cycle.getCycleThread() != null && cycle.getCycleThread().isAlive()) {
			cycle.stopCycle();
			cycle = new AutosellCycle(this);
		}

		cycle.cycleSetup(args);
		lastRestartTime = System.currentTimeMillis();
	}

	private void stopCycle() {
		if (cycle != null) {
			cycle.stopCycle();
			cycle = null;
		}
	}

	private void restartCycle() {
		stopCycle();
		startCycle();
	}

	public AutosellCycle getCycle() {
		return cycle;
	}

	public void setSwitches(int newswitches) {
		switches = newswitches;
	}

	public int getSwitches() {
		return switches;
	}

	public ArrayList<String> getArgs() {
		return args;
	}

	public List<String> getLogins() {
		return logins;
	}

	public List<String> getPasswords() {
		return passwords;
	}

	public int getTryes() {
		return tryes;
	}

	public void setTryes(int tryes) {
		this.tryes = tryes;
	}

	public int getLoops() {
		return loops;
	}

	public void setLoops(int loops) {
		this.loops = loops;
	}

	public void loopsPlusPlus() {
		loops++;
	}

    public void setArgs(ArrayList<String> args) {
        this.args = args;
    }
}
