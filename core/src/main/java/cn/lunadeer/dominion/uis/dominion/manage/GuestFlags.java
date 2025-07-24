package cn.lunadeer.dominion.uis.dominion.manage;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.api.dtos.flag.PriFlag;
import cn.lunadeer.dominion.commands.DominionFlagCommand;
import cn.lunadeer.dominion.configuration.Language;
import cn.lunadeer.dominion.configuration.uis.ChestUserInterface;
import cn.lunadeer.dominion.configuration.uis.TextUserInterface;
import cn.lunadeer.dominion.misc.CommandArguments;
import cn.lunadeer.dominion.uis.AbstractUI;
import cn.lunadeer.dominion.uis.MainMenu;
import cn.lunadeer.dominion.uis.dominion.DominionList;
import cn.lunadeer.dominion.uis.dominion.DominionManage;
import cn.lunadeer.dominion.utils.Notification;
import cn.lunadeer.dominion.utils.command.SecondaryCommand;
import cn.lunadeer.dominion.utils.configuration.ConfigurationPart;
import cn.lunadeer.dominion.utils.scui.ChestButton;
import cn.lunadeer.dominion.utils.scui.ChestListView;
import cn.lunadeer.dominion.utils.scui.ChestUserInterfaceManager;
import cn.lunadeer.dominion.utils.scui.configuration.ButtonConfiguration;
import cn.lunadeer.dominion.utils.scui.configuration.ListViewConfiguration;
import cn.lunadeer.dominion.utils.stui.ListView;
import cn.lunadeer.dominion.utils.stui.components.Line;
import cn.lunadeer.dominion.utils.stui.components.buttons.FunctionalButton;
import cn.lunadeer.dominion.utils.stui.components.buttons.ListViewButton;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

import static cn.lunadeer.dominion.Dominion.defaultPermission;
import static cn.lunadeer.dominion.misc.Asserts.assertDominionAdmin;
import static cn.lunadeer.dominion.misc.Converts.toDominionDTO;
import static cn.lunadeer.dominion.misc.Converts.toIntegrity;
import static cn.lunadeer.dominion.utils.Misc.*;

public class GuestFlags extends AbstractUI {

    public static void show(CommandSender sender, String dominionName, String pageStr) {
        new GuestFlags().displayByPreference(sender, dominionName, pageStr);
    }

    public static SecondaryCommand flags = new SecondaryCommand("guest_flags", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.OptionalPageArgument()
    ), Language.uiCommandsDescription.guestFlags) {
        @Override
        public void executeHandler(CommandSender sender) {
            show(sender, getArgumentValue(0), getArgumentValue(1));
        }
    }.needPermission(defaultPermission).register();

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ TUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class GuestSettingTuiText extends ConfigurationPart {
        public String title = "{0} Guest Setting";
        public String button = "GUEST SET";
        public String description = "Set guest behavior of dominion.";
    }

    public static ListViewButton button(CommandSender sender, String dominionName) {
        return (ListViewButton) new ListViewButton(TextUserInterface.guestSettingTuiText.button) {
            @Override
            public void function(String pageStr) {
                show(sender, dominionName, pageStr);
            }
        }.needPermission(defaultPermission);
    }

    @Override
    protected void showTUI(Player player, String... args) {
        String dominionName = args[0];
        DominionDTO dominion = toDominionDTO(dominionName);
        assertDominionAdmin(player, dominion);
        int page = toIntegrity(args[1]);

        ListView view = ListView.create(10, button(player, dominionName));
        view.title(formatString(TextUserInterface.guestSettingTuiText.title, dominion.getName()))
                .navigator(Line.create()
                        .append(MainMenu.button(player).build())
                        .append(DominionList.button(player).build())
                        .append(DominionManage.button(player, dominionName).build())
                        .append(TextUserInterface.guestSettingTuiText.button));
        for (PriFlag flag : Flags.getAllPriFlagsEnable()) {
            if (flag.equals(Flags.ADMIN)) continue; // Skip admin flag this only for group or member
            if (dominion.getGuestFlagValue(flag)) {
                view.add(Line.create()
                        .append(new FunctionalButton("☑") {
                            @Override
                            public void function() {
                                DominionFlagCommand.setGuest(player, dominionName, flag.getFlagName(), "false", String.valueOf(page));
                            }
                        }.green().build())
                        .append(Component.text(flag.getDisplayName()).hoverEvent(Component.text(flag.getDescription())))
                );
            } else {
                view.add(Line.create()
                        .append(new FunctionalButton("☐") {
                            @Override
                            public void function() {
                                DominionFlagCommand.setGuest(player, dominionName, flag.getFlagName(), "true", String.valueOf(page));
                            }
                        }.red().build())
                        .append(Component.text(flag.getDisplayName()).hoverEvent(Component.text(flag.getDescription())))
                );
            }
        }
        view.showOn(player, page);
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ TUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ CUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class GuestSettingCui extends ConfigurationPart {
        public String title = "§6✦ §e§lGuest Setting of {0} §6✦";
        public ListViewConfiguration listConfiguration = new ListViewConfiguration(
                'i',
                List.of(
                        "<########",
                        "#iiiiiii#",
                        "#iiiiiii#",
                        "#iiiiiii#",
                        "p#######n"
                )
        );

        public ButtonConfiguration backButton = ButtonConfiguration.createMaterial(
                '<', Material.RED_STAINED_GLASS_PANE,
                "§c« Back to Dominion Management",
                List.of(
                        "§7Return to the dominion",
                        "§7management menu.",
                        "",
                        "§e▶ Click to go back"
                )
        );

        public String flagItemName = "§6🚪 §e{0}";
        public String flagItemStateTrue = "§a§l✓ ALLOWED";
        public String flagItemStateFalse = "§c§l✗ DENIED";
        public List<String> flagItemLore = List.of(
                "§7Guest Permission: {0}",
                "",
                "§7Description:",
                "§f{1}",
                "§f{2}",
                "",
                "§e▶ Click to toggle for guests",
                "§8Affects non-member visitors"
        );
    }

    @Override
    protected void showCUI(Player player, String... args) {
        DominionDTO dominion = toDominionDTO(args[0]);
        assertDominionAdmin(player, dominion);

        ChestListView view = ChestUserInterfaceManager.getInstance().getListViewOf(player);
        view.setTitle(formatString(ChestUserInterface.guestSettingCui.title, dominion.getName()));
        view.applyListConfiguration(ChestUserInterface.guestSettingCui.listConfiguration, toIntegrity(args[1]));

        view.setButton(ChestUserInterface.guestSettingCui.backButton.getSymbol(),
                new ChestButton(ChestUserInterface.guestSettingCui.backButton) {
                    @Override
                    public void onClick(ClickType type) {
                        DominionManage.show(player, dominion.getName(), "1");
                    }
                }
        );

        for (int i = 0; i < Flags.getAllPriFlagsEnable().size(); i++) {
            PriFlag flag = Flags.getAllPriFlagsEnable().get(i);
            if (flag.equals(Flags.ADMIN)) continue; // Skip admin flag this only for group or member
            Integer page = (int) Math.ceil((double) (i + 1) / view.getPageSize());
            String flagState = dominion.getGuestFlagValue(flag) ? ChestUserInterface.guestSettingCui.flagItemStateTrue : ChestUserInterface.guestSettingCui.flagItemStateFalse;
            String flagName = formatString(ChestUserInterface.guestSettingCui.flagItemName, flag.getDisplayName());
            List<String> descriptions = foldLore2Line(flag.getDescription(), 30);
            List<String> flagLore = formatStringList(ChestUserInterface.guestSettingCui.flagItemLore, flagState, descriptions.get(0), descriptions.get(1));
            ButtonConfiguration btnConfig = ButtonConfiguration.createMaterial(
                    ChestUserInterface.guestSettingCui.listConfiguration.itemSymbol.charAt(0),
                    flag.getMaterial(),
                    flagName,
                    flagLore
            );
            view.addItem(new ChestButton(btnConfig) {
                @Override
                public void onClick(ClickType type) {
                    DominionFlagCommand.setGuest(player, dominion.getName(), flag.getFlagName(), String.valueOf(!dominion.getGuestFlagValue(flag)), page.toString());
                }
            });
        }

        view.open();
    }


    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ CUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ Console ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    @Override
    protected void showConsole(CommandSender sender, String... args) throws Exception {
        Notification.info(sender, ChestUserInterface.guestSettingCui.title, args[0]);

        Notification.info(sender, DominionFlagCommand.SetGuestFlag.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, DominionFlagCommand.SetGuestFlag.getDescription());

        DominionDTO dominion = toDominionDTO(args[0]);
        int page = toIntegrity(args[2], 1);
        Triple<Integer, Integer, Integer> pageInfo = pageUtil(page, 15, Flags.getAllPriFlagsEnable().size());
        for (int i = pageInfo.getLeft(); i < pageInfo.getMiddle(); i++) {
            PriFlag flag = Flags.getAllPriFlagsEnable().get(i);
            String flagState = dominion.getGuestFlagValue(flag) ? ChestUserInterface.guestSettingCui.flagItemStateTrue : ChestUserInterface.guestSettingCui.flagItemStateFalse;
            String flagName = formatString(ChestUserInterface.guestSettingCui.flagItemName, flag.getDisplayName());
            String item = "§6▶ " + flagName + "\t" + flagState + "\t" + flag.getDescription();
            Notification.info(sender, item);
        }

        Notification.info(sender, Language.consoleText.pageInfo, page, pageInfo.getRight(), Flags.getAllPriFlagsEnable().size());
    }
}
