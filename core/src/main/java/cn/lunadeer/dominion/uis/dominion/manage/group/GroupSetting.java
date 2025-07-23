package cn.lunadeer.dominion.uis.dominion.manage.group;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.api.dtos.flag.PriFlag;
import cn.lunadeer.dominion.commands.GroupCommand;
import cn.lunadeer.dominion.configuration.Language;
import cn.lunadeer.dominion.configuration.uis.ChestUserInterface;
import cn.lunadeer.dominion.configuration.uis.TextUserInterface;
import cn.lunadeer.dominion.inputters.RenameGroupInputter;
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
import static cn.lunadeer.dominion.misc.Converts.*;
import static cn.lunadeer.dominion.utils.Misc.*;

public class GroupSetting extends AbstractUI {

    public static void show(CommandSender sender, String dominionName, String groupName, String pageStr) {
        new GroupSetting().displayByPreference(sender, dominionName, groupName, pageStr);
    }

    public static SecondaryCommand flags = new SecondaryCommand("group_flags", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.RequiredGroupArgument(0),
            new CommandArguments.OptionalPageArgument()
    )) {
        @Override
        public void executeHandler(CommandSender sender) {
            show(sender, getArgumentValue(0), getArgumentValue(1), getArgumentValue(2));
        }
    }.needPermission(defaultPermission).register();

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ TUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class GroupSettingTuiText extends ConfigurationPart {
        public String title = "Group {0} Settings";
        public String description = "Manage the settings of group {0}.";
        public String button = "SETTING";
    }

    public static ListViewButton button(CommandSender sender, String dominionName, String groupName) {
        return (ListViewButton) new ListViewButton(TextUserInterface.groupSettingTuiText.button) {
            @Override
            public void function(String page) {
                show(sender, dominionName, groupName, page);
            }
        }.needPermission(defaultPermission).setHoverText(TextUserInterface.groupSettingTuiText.description);
    }

    @Override
    protected void showTUI(Player player, String... args) {
        DominionDTO dominion = toDominionDTO(args[0]);
        assertDominionAdmin(player, dominion);
        GroupDTO group = toGroupDTO(dominion, args[1]);
        int page = toIntegrity(args[2], 1);

        ListView view = ListView.create(10, button(player, dominion.getName(), group.getNamePlain()));
        view.title(formatString(TextUserInterface.groupSettingTuiText.title, group.getNameColoredBukkit()));
        view.navigator(
                Line.create()
                        .append(MainMenu.button(player).build())
                        .append(DominionList.button(player).build())
                        .append(DominionManage.button(player, dominion.getName()).build())
                        .append(GroupList.button(player, dominion.getName()).build())
                        .append(TextUserInterface.groupSettingTuiText.button)
        );
        view.add(Line.create().append(RenameGroupInputter.createTuiButtonOn(player, dominion.getName(), group.getNamePlain()).build()));

        if (group.getFlagValue(Flags.ADMIN)) {
            view.add(createOption(player, Flags.ADMIN, true, dominion.getName(), group.getNamePlain(), args[2]));
            view.add(createOption(player, Flags.GLOW, group.getFlagValue(Flags.GLOW), dominion.getName(), group.getNamePlain(), args[2]));
        } else {
            for (PriFlag flag : Flags.getAllPriFlagsEnable()) {
                view.add(createOption(player, flag, group.getFlagValue(flag), dominion.getName(), group.getNamePlain(), args[2]));
            }
        }
        view.showOn(player, page);
    }

    private static Line createOption(Player player, PriFlag flag, boolean value, String DominionName, String groupName, String pageStr) {
        if (value) {
            return Line.create()
                    .append(new FunctionalButton("☑") {
                        @Override
                        public void function() {
                            GroupCommand.setGroupFlag(player, DominionName, groupName, flag.getFlagName(), "false", pageStr);
                        }
                    }.needPermission(defaultPermission).green().build())
                    .append(Component.text(flag.getDisplayName()).hoverEvent(Component.text(flag.getDescription())));
        } else {
            return Line.create()
                    .append(new FunctionalButton("☐") {
                        @Override
                        public void function() {
                            GroupCommand.setGroupFlag(player, DominionName, groupName, flag.getFlagName(), "true", pageStr);
                        }
                    }.needPermission(defaultPermission).red().build())
                    .append(Component.text(flag.getDisplayName()).hoverEvent(Component.text(flag.getDescription())));
        }
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ TUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ CUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class GroupSettingCui extends ConfigurationPart {
        public String title = "§6✦ §9§lGroup {0} Settings §6✦";
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
                "Back",
                List.of(
                        "Go back to the",
                        "guest manage."
                )
        );

        public String flagItemName = "&7Flag: &9{0}";
        public String flagItemStateTrue = "&a&l[ENABLED]";
        public String flagItemStateFalse = "&c&l[DISABLED]";
        public List<String> flagItemLore = List.of(
                "&7State: {0}",
                "&7Des: &f{1}",
                "       &f{2}",
                "",
                "&7Click to toggle this flag."
        );
    }

    @Override
    protected void showCUI(Player player, String... args) {
        DominionDTO dominion = toDominionDTO(args[0]);
        assertDominionAdmin(player, dominion);
        GroupDTO group = toGroupDTO(dominion, args[1]);
        ChestListView view = ChestUserInterfaceManager.getInstance().getListViewOf(player);
        view.setTitle(formatString(ChestUserInterface.groupSettingCui.title, group.getNameColoredBukkit()));
        view.applyListConfiguration(ChestUserInterface.groupSettingCui.listConfiguration, toIntegrity(args[2]));

        view.setButton(ChestUserInterface.groupSettingCui.backButton.getSymbol(),
                new ChestButton(ChestUserInterface.groupSettingCui.backButton) {
                    @Override
                    public void onClick(ClickType type) {
                        GroupManage.show(player, dominion.getName(), group.getNamePlain(), "1");
                    }
                }
        );

        for (int i = 0; i < Flags.getAllPriFlagsEnable().size(); i++) {
            PriFlag flag = Flags.getAllPriFlagsEnable().get(i);
            Integer page = (int) Math.ceil((double) (i + 1) / view.getPageSize());
            String flagState = group.getFlagValue(flag) ? ChestUserInterface.groupSettingCui.flagItemStateTrue : ChestUserInterface.groupSettingCui.flagItemStateFalse;
            String flagName = formatString(ChestUserInterface.groupSettingCui.flagItemName, flag.getDisplayName());
            List<String> descriptions = foldLore2Line(flag.getDescription(), 30);
            List<String> flagLore = formatStringList(ChestUserInterface.groupSettingCui.flagItemLore, flagState, descriptions.get(0), descriptions.get(1));
            ButtonConfiguration btnConfig = ButtonConfiguration.createMaterial(
                    ChestUserInterface.groupSettingCui.listConfiguration.itemSymbol.charAt(0),
                    flag.getMaterial(),
                    flagName,
                    flagLore
            );
            view.addItem(new ChestButton(btnConfig) {
                @Override
                public void onClick(ClickType type) {
                    GroupCommand.setGroupFlag(player, dominion.getName(), group.getNamePlain(), flag.getFlagName(), String.valueOf(!group.getFlagValue(flag)), String.valueOf(page));
                }
            });
        }

        view.open();
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ CUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ Console ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    @Override
    protected void showConsole(CommandSender sender, String... args) throws Exception {
        Notification.info(sender, ChestUserInterface.groupSettingCui.title);
        // command
        Notification.info(sender, GroupCommand.setGroupFlag.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupCommand.setGroupFlag.getDescription());
        // items
        DominionDTO dominion = toDominionDTO(args[0]);
        GroupDTO group = toGroupDTO(dominion, args[1]);
        int page = toIntegrity(args[2], 1);
        Triple<Integer, Integer, Integer> pageInfo = pageUtil(page, 15, Flags.getAllPriFlagsEnable().size());
        for (int i = pageInfo.getLeft(); i < pageInfo.getMiddle(); i++) {
            PriFlag flag = Flags.getAllPriFlagsEnable().get(i);
            String flagState = group.getFlagValue(flag) ? ChestUserInterface.groupSettingCui.flagItemStateTrue : ChestUserInterface.groupSettingCui.flagItemStateFalse;
            String flagName = formatString(ChestUserInterface.groupSettingCui.flagItemName, flag.getDisplayName());
            String item = "§6▶ " + flagName + "\t" + flagState + "\t" + flag.getDescription();
            Notification.info(sender, item);
        }
        // page info
        Notification.info(sender, Language.consoleText.pageInfo, page, pageInfo.getRight(), Flags.getAllPriFlagsEnable().size());
    }
}
