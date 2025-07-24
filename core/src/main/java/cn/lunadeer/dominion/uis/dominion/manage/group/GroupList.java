package cn.lunadeer.dominion.uis.dominion.manage.group;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.PlayerDTO;
import cn.lunadeer.dominion.commands.GroupCommand;
import cn.lunadeer.dominion.configuration.Language;
import cn.lunadeer.dominion.configuration.uis.ChestUserInterface;
import cn.lunadeer.dominion.configuration.uis.TextUserInterface;
import cn.lunadeer.dominion.doos.GroupDOO;
import cn.lunadeer.dominion.inputters.CreateGroupInputter;
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
import cn.lunadeer.dominion.utils.stui.components.buttons.Button;
import cn.lunadeer.dominion.utils.stui.components.buttons.FunctionalButton;
import cn.lunadeer.dominion.utils.stui.components.buttons.ListViewButton;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

import static cn.lunadeer.dominion.Dominion.defaultPermission;
import static cn.lunadeer.dominion.doos.MemberDOO.selectByDominionId;
import static cn.lunadeer.dominion.misc.Asserts.assertDominionAdmin;
import static cn.lunadeer.dominion.misc.Converts.*;
import static cn.lunadeer.dominion.utils.Misc.formatString;
import static cn.lunadeer.dominion.utils.Misc.pageUtil;

public class GroupList extends AbstractUI {

    public static void show(CommandSender sender, String dominionName, String pageStr) {
        new GroupList().displayByPreference(sender, dominionName, pageStr);
    }

    public static SecondaryCommand list = new SecondaryCommand("group_list", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.OptionalPageArgument()
    ), Language.uiCommandsDescription.groupList) {
        @Override
        public void executeHandler(CommandSender sender) {
            show(sender, getArgumentValue(0), getArgumentValue(1));
        }
    }.needPermission(defaultPermission).register();

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ TUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class GroupListTuiText extends ConfigurationPart {
        public String title = "{0} Group List";
        public String description = "List of groups of this dominion.";
        public String button = "GROUPS";
        public String deleteButton = "DELETE";
        public String deleteDescription = "Delete this group, all members will be move out of this group.";
        public String removeMemberDescription = "Remove {0} from group {1} to default group.";
    }

    public static ListViewButton button(CommandSender sender, String dominionName) {
        return (ListViewButton) new ListViewButton(TextUserInterface.groupListTuiText.button) {
            @Override
            public void function(String page) {
                show(sender, dominionName, page);
            }
        }.needPermission(defaultPermission);
    }

    @Override
    protected void showTUI(Player player, String... args) throws Exception {
        String dominionName = args[0];
        String pageStr = args[1];
        DominionDTO dominion = toDominionDTO(dominionName);
        assertDominionAdmin(player, dominion);
        int page = toIntegrity(pageStr);

        List<GroupDOO> groups = GroupDOO.selectByDominionId(dominion.getId());

        ListView view = ListView.create(10, button(player, dominionName));
        view.title(formatString(TextUserInterface.groupListTuiText.title, dominion.getName()));
        view.navigator(
                Line.create()
                        .append(MainMenu.button(player).build())
                        .append(DominionList.button(player).build())
                        .append(DominionManage.button(player, dominionName).build())
                        .append(TextUserInterface.groupListTuiText.button)
        );
        view.add(new Line()
                .append(CreateGroupInputter.createTuiButtonOn(player, dominionName).needPermission(defaultPermission).build())
        );

        // get data from database directly because cache update may not be in time
        List<MemberDTO> members = new ArrayList<>(selectByDominionId(dominion.getId()));
        for (GroupDTO group : groups) {
            Line line = new Line();
            Button deleteGroup = new FunctionalButton(TextUserInterface.groupListTuiText.deleteButton) {
                @Override
                public void function() {
                    GroupCommand.deleteGroup(player, dominionName, group.getNamePlain(), pageStr);
                }
            }.needPermission(defaultPermission).red().setHoverText(TextUserInterface.groupListTuiText.deleteDescription);
            Button setting = GroupFlags.button(player, dominionName, group.getNamePlain());
            Button addMember = SelectMember.button(player, dominionName, group.getNamePlain(), pageStr);
            line.append(deleteGroup.build()).append(setting.build()).append(group.getNameColoredComponent()).append(addMember.build());
            view.add(line);
            for (MemberDTO member : members) {
                if (!member.getGroupId().equals(group.getId())) {
                    continue;
                }
                PlayerDTO p = toPlayerDTO(member.getPlayerUUID());
                Button remove = new FunctionalButton("-") {
                    @Override
                    public void function() {
                        GroupCommand.removeMember(player, dominionName, group.getNamePlain(), p.getLastKnownName(), pageStr);
                    }
                }.needPermission(defaultPermission).red().setHoverText(TextUserInterface.groupListTuiText.removeMemberDescription);
                Line playerLine = new Line().setDivider("");
                playerLine.append(Component.text("        "));
                playerLine.append(remove.build()).append(" |  " + p.getLastKnownName());
                view.add(playerLine);
            }
            view.add(new Line().append(""));
        }

        view.showOn(player, page);
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ TUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ CUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class GroupListCui extends ConfigurationPart {
        public String title = "§6✦ §5§lGroup List of {0} §6✦";
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

        public ButtonConfiguration newGroupButton = ButtonConfiguration.createMaterial(
                'i', Material.LIME_DYE,
                "§a➕ §2Create New Group",
                List.of(
                        "§7Create a new permission group",
                        "§7for organizing your members.",
                        "",
                        "§2▶ Click to create group",
                        "",
                        "§8Perfect for ranks and roles!"
                )
        );

        public ButtonConfiguration groupItemButton = ButtonConfiguration.createMaterial(
                'i', Material.CHEST,
                "§6👑 §e{0}",
                List.of(
                        "§7Members: §a{0}",
                        "",
                        "§e▶ Click to manage this group",
                        "§8  Edit permissions, add members...",
                        "",
                        "§7Type: §6Permission Group"
                )
        );
    }

    @Override
    protected void showCUI(Player player, String... args) throws Exception {
        String dominionName = args[0];
        DominionDTO dominion = toDominionDTO(dominionName);
        assertDominionAdmin(player, dominion);
        ChestListView view = ChestUserInterfaceManager.getInstance().getListViewOf(player);
        view.setTitle(formatString(ChestUserInterface.groupListCui.title, dominion.getName()));
        view.applyListConfiguration(ChestUserInterface.groupListCui.listConfiguration, toIntegrity(args[1]));

        view.setButton(ChestUserInterface.groupListCui.backButton.getSymbol(),
                new ChestButton(ChestUserInterface.groupListCui.backButton) {
                    @Override
                    public void onClick(ClickType type) {
                        DominionManage.show(player, dominion.getName(), "1");
                    }
                }
        );

        view.addItem(new ChestButton(ChestUserInterface.groupListCui.newGroupButton) {
            @Override
            public void onClick(ClickType type) {
                CreateGroupInputter.createOn(player, dominion.getName());
                view.close();
            }
        });

        for (GroupDTO group : GroupDOO.selectByDominionId(dominion.getId())) {
            ChestButton groupChest = new ChestButton(ChestUserInterface.groupListCui.groupItemButton) {
                @Override
                public void onClick(ClickType type) {
                    GroupManage.show(player, dominion.getName(), group.getNamePlain(), "1");
                }
            }.setDisplayNameArgs(group.getNameColoredBukkit()).setLoreArgs(group.getMembers().size());
            view.addItem(groupChest);
        }

        view.open();
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ CUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ Console ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    @Override
    protected void showConsole(CommandSender sender, String... args) throws Exception {
        Notification.info(sender, ChestUserInterface.groupListCui.title, args[0]);
        // command
        Notification.info(sender, GroupCommand.createGroup.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupCommand.createGroup.getDescription());
        Notification.info(sender, GroupCommand.deleteGroup.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupCommand.deleteGroup.getDescription());
        Notification.info(sender, GroupCommand.renameGroup.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupCommand.renameGroup.getDescription());
        Notification.info(sender, GroupCommand.addMember.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupCommand.addMember.getDescription());
        Notification.info(sender, GroupCommand.removeMember.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupCommand.removeMember.getDescription());
        Notification.info(sender, GroupFlags.flags.getUsage());
        Notification.info(sender, Language.consoleText.descPrefix, GroupFlags.flags.getDescription());
        // items
        DominionDTO dominion = toDominionDTO(args[0]);
        List<GroupDOO> groups = GroupDOO.selectByDominionId(dominion.getId());
        int page = toIntegrity(args[1], 1);
        int totalSize = groups.size();
        List<String> lines = new ArrayList<>();
        for (GroupDOO group : groups) {
            totalSize += group.getMembers().size();
            String groupName = group.getNameColoredBukkit();
            int memberCount = group.getMembers().size();
            lines.add("§6▶ §6" + groupName + " §7(" + memberCount + ")");
            for (MemberDTO member : group.getMembers()) {
                PlayerDTO playerDTO = toPlayerDTO(member.getPlayerUUID());
                lines.add("§6▶   - §a" + playerDTO.getLastKnownName());
            }
        }
        Triple<Integer, Integer, Integer> pageInfo = pageUtil(page, 10, totalSize);
        for (int i = pageInfo.getLeft(); i < pageInfo.getMiddle(); i++) {
            Notification.info(sender, lines.get(i));
        }
        // page info
        Notification.info(sender, Language.consoleText.pageInfo, page, pageInfo.getRight(), totalSize);
    }

}
