package com.github.neapovil.systeminfo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.neapovil.systeminfo.command.Command;

import net.kyori.adventure.text.Component;

public final class SystemInfo extends JavaPlugin
{
    private static SystemInfo instance;
    private final oshi.SystemInfo systemInfo = new oshi.SystemInfo();
    private final List<Component> disks = new ArrayList<>();
    private Component cpuInfo;

    @Override
    public void onEnable()
    {
        instance = this;

        this.loadDisks();
        this.loadCpu();

        Command.register();
    }

    @Override
    public void onDisable()
    {
    }

    public static SystemInfo getInstance()
    {
        return instance;
    }

    public oshi.SystemInfo getSystemInfo()
    {
        return this.systemInfo;
    }

    public List<Component> getDisks()
    {
        return this.disks;
    }

    public final String formatBytesToGigabytes(long bytes)
    {
        return new DecimalFormat("##.0").format(bytes / 1024.0 / 1024.0 / 1024.0) + " GB";
    }

    public Component getCpuInfo()
    {
        return this.cpuInfo;
    }

    private final void loadDisks()
    {
        this.systemInfo
                .getHardware()
                .getDiskStores()
                .forEach(disk -> {
                    disk.getPartitions()
                            .stream()
                            .map(partition -> partition.getUuid())
                            .forEach(partitionUUID -> {
                                this.systemInfo.getOperatingSystem()
                                        .getFileSystem()
                                        .getFileStores()
                                        .stream()
                                        .filter(osFileStore -> osFileStore.getUUID().equals(partitionUUID))
                                        .findAny()
                                        .ifPresent(osFileStore -> {
                                            final Component component = Component.text("(" + osFileStore.getMount() + ")")
                                                    .append(Component.text(" " + disk.getModel()));
                                            final Component component1 = Component
                                                    .text("Total Space: " + this.formatBytesToGigabytes(osFileStore.getTotalSpace()))
                                                    .append(Component.text("\nFree Space: " + this.formatBytesToGigabytes(osFileStore.getFreeSpace())))
                                                    .append(Component.text("\nUsable Space: " + this.formatBytesToGigabytes(osFileStore.getUsableSpace())))
                                                    .append(Component.text("\nT: " + osFileStore.toString()));

                                            disks.add(component.hoverEvent(component1));
                                        });
                            });
                });
    }

    private final void loadCpu()
    {
        this.cpuInfo = Component.text("CPU Name: " + this.systemInfo.getHardware().getProcessor().getProcessorIdentifier().getName())
                .append(Component.text("\nCPU Cores: " + this.systemInfo.getHardware().getProcessor().getPhysicalProcessorCount()))
                .append(Component.text("\nCPU Threads: " + this.systemInfo.getHardware().getProcessor().getLogicalProcessorCount()));
    }
}
