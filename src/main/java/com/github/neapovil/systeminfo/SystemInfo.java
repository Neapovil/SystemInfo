package com.github.neapovil.systeminfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.text.Component;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.util.FormatUtil;

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

        this.populateDisks();
        this.populateCpu();

        new CommandAPICommand("systeminfo")
                .withPermission("systeminfo.command")
                .withArguments(new MultiLiteralArgument("option", "os", "cpu", "ram", "uptime", "disks"))
                .executes((sender, args) -> {
                    final String option = (String) args.get("option");

                    if (option.equals("os"))
                    {
                        sender.sendMessage("OS: " + this.systemInfo.getOperatingSystem().toString());
                    }

                    if (option.equals("cpu"))
                    {
                        sender.sendMessage(this.cpuInfo);
                    }

                    if (option.equals("ram"))
                    {
                        sender.sendMessage("RAM Total: " + FormatUtil.formatBytes(this.systemInfo.getHardware().getMemory().getTotal()));
                        sender.sendMessage("RAM Available: " + FormatUtil.formatBytes(this.systemInfo.getHardware().getMemory().getAvailable()));
                    }

                    if (option.equals("uptime"))
                    {
                        final Duration duration = Duration.ofSeconds(this.systemInfo.getOperatingSystem().getSystemUptime());

                        sender.sendMessage("System Uptime: " + FormatUtil.formatElapsedSecs(duration.toSeconds()));

                        final OSProcess osprocess = this.systemInfo.getOperatingSystem()
                                .getProcess(this.systemInfo.getOperatingSystem().getProcessId());
                        final Duration duration1 = Duration.ofMillis(osprocess.getUpTime());

                        sender.sendMessage("Server Uptime: " + FormatUtil.formatElapsedSecs(duration1.toSeconds()));
                    }

                    if (option.equals("disks"))
                    {
                        sender.sendMessage("Disks:");
                        this.disks.forEach(i -> sender.sendMessage(i));
                    }
                })
                .register();
    }

    @Override
    public void onDisable()
    {
    }

    public static SystemInfo instance()
    {
        return instance;
    }

    private final void populateDisks()
    {
        for (HWDiskStore disk : this.systemInfo.getHardware().getDiskStores())
        {
            for (HWPartition partition : disk.getPartitions())
            {
                for (OSFileStore store : this.systemInfo.getOperatingSystem().getFileSystem().getFileStores())
                {
                    if (!store.getUUID().equals(partition.getUuid()))
                    {
                        continue;
                    }

                    final Component component = Component.text("(" + store.getMount() + ") " + disk.getModel());
                    final Component component1 = Component
                            .text("Total Space: " + FormatUtil.formatBytesDecimal(store.getTotalSpace()))
                            .append(Component.text("\nFree Space: " + FormatUtil.formatBytesDecimal(store.getFreeSpace())))
                            .append(Component.text("\nUsable Space: " + FormatUtil.formatBytesDecimal(store.getUsableSpace())));

                    this.disks.add(component.hoverEvent(component1));
                }
            }
        }
    }

    private void populateCpu()
    {
        this.cpuInfo = Component.text("CPU Name: " + this.systemInfo.getHardware().getProcessor().getProcessorIdentifier().getName())
                .append(Component.text("\nCPU Cores: " + this.systemInfo.getHardware().getProcessor().getPhysicalProcessorCount()))
                .append(Component.text("\nCPU Threads: " + this.systemInfo.getHardware().getProcessor().getLogicalProcessorCount()));
    }
}
