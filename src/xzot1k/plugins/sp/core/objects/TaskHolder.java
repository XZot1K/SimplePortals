package xzot1k.plugins.sp.core.objects;

import org.bukkit.scheduler.BukkitTask;

public class TaskHolder {

	private BukkitTask selectionPointOne, selectionPointTwo, regionDisplay;

	public BukkitTask getSelectionPointOne() {
		return selectionPointOne;
	}

	public void setSelectionPointOne(BukkitTask selectionPointOne) {
		if (this.selectionPointOne != null)
			this.selectionPointOne.cancel();
		this.selectionPointOne = selectionPointOne;
	}

	public BukkitTask getSelectionPointTwo() {
		return selectionPointTwo;
	}

	public void setSelectionPointTwo(BukkitTask selectionPointTwo) {
		if (this.selectionPointTwo != null)
			this.selectionPointTwo.cancel();
		this.selectionPointTwo = selectionPointTwo;
	}

	public BukkitTask getRegionDisplay() {
		return regionDisplay;
	}

	public void setRegionDisplay(BukkitTask regionDisplay) {
		if (this.regionDisplay != null)
			this.regionDisplay.cancel();
		this.regionDisplay = regionDisplay;
	}
}
