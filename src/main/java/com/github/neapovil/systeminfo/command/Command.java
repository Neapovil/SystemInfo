package com.github.neapovil.systeminfo.command;

import java.time.Duration;

import com.github.neapovil.systeminfo.SystemInfo;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import oshi.software.os.OSProcess;

public final class Command
{
    private static final SystemInfo plugin = SystemInfo.getInstance();

    public static final void register()
    {
        new CommandAPICommand("systeminfo")
                .withPermission("systeminfo.command")
                .withArguments(new MultiLiteralArgument("os", "cpu", "ram", "uptime", "disks"))
                .executes((sender, args) -> {
                    final String option = (String) args[0];

                    if (option.equals("os"))
                    {
                        sender.sendMessage("OS: " + plugin.getSystemInfo().getOperatingSystem().toString());
                        return;
                    }

                    if (option.equals("cpu"))
                    {
                        sender.sendMessage(plugin.getCpuInfo());
                        return;
                    }

                    if (option.equals("ram"))
                    {
                        sender.sendMessage("RAM Total: " + plugin.formatBytesToGigabytes(plugin.getSystemInfo().getHardware().getMemory().getTotal()));
                        sender.sendMessage("RAM Available: " + plugin.formatBytesToGigabytes(plugin.getSystemInfo().getHardware().getMemory().getAvailable()));
                        return;
                    }

                    if (option.equals("uptime"))
                    {
                        final Duration duration = Duration.ofSeconds(plugin.getSystemInfo().getOperatingSystem().getSystemUptime());

                        sender.sendMessage("System Uptime: " + formatUptime(duration));

                        final OSProcess osprocess = plugin.getSystemInfo().getOperatingSystem()
                                .getProcess(plugin.getSystemInfo().getOperatingSystem().getProcessId());
                        final Duration duration1 = Duration.ofMillis(osprocess.getUpTime());

                        sender.sendMessage("Server Uptime: " + formatUptime(duration1));
                        return;
                    }

                    if (option.equals("disks"))
                    {
                        sender.sendMessage("Disks:");

                        plugin.getDisks().forEach(i -> {
                            sender.sendMessage(i);
                        });

                        return;
                    }
                })
                .register();
    }

    private static final String formatUptime(Duration duration)
    {
        return String.format("%2d:%02d:%02d", duration.toHours() % 3600,
                duration.toMinutes() % 60, duration.toSeconds() % 60);
    }
}
