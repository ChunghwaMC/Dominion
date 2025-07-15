package cn.lunadeer.dominion.uis;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.PlayerDTO;
import cn.lunadeer.dominion.cache.CacheManager;
import cn.lunadeer.dominion.commands.GroupTitleCommand;
import cn.lunadeer.dominion.configuration.ChestUserInterface;
import cn.lunadeer.dominion.configuration.Language;
import cn.lunadeer.dominion.misc.CommandArguments;
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
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

import static cn.lunadeer.dominion.Dominion.defaultPermission;
import static cn.lunadeer.dominion.misc.Converts.toIntegrity;
import static cn.lunadeer.dominion.misc.Converts.toPlayer;
import static cn.lunadeer.dominion.utils.Misc.formatString;

public class TitleList extends AbstractUI {

    public static void show(CommandSender sender, String pageStr) {
        new TitleList().displayByPreference(sender, pageStr);
    }

    public static SecondaryCommand titleList = new SecondaryCommand("title_list", List.of(
            new CommandArguments.OptionalPageArgument()
    )) {
        @Override
        public void executeHandler(CommandSender sender) {
            try {
                TitleList.show(sender, getArgumentValue(0));
            } catch (Exception e) {
                Notification.error(sender, e.getMessage());
            }
        }
    }.needPermission(defaultPermission).register();

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ TUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class TitleListTuiText extends ConfigurationPart {
        public String title = "Group Title List";
        public String description = "List of group titles you can use.";
        public String button = "TITLES";
        public String useButton = "USE";
        public String disuseButton = "DISUSE";
        public String fromDominion = "From dominion {0}";
    }

    public static ListViewButton button(CommandSender sender) {
        return (ListViewButton) new ListViewButton(Language.titleListTuiText.button) {
            @Override
            public void function(String pageStr) {
                TitleList.show(sender, pageStr);
            }
        }.needPermission(defaultPermission);
    }

    @Override
    protected void showTUI(CommandSender sender, String... args) throws Exception {
        Player player = toPlayer(sender);
        int page = toIntegrity(args[0], 1);

        ListView view = ListView.create(10, button(sender));

        view.title(Language.titleListTuiText.title);
        view.navigator(Line.create()
                .append(MainMenu.button(sender).build())
                .append(Language.titleListTuiText.button));

        List<GroupDTO> groups = CacheManager.instance.getPlayerCache().getPlayerGroupTitleList(player.getUniqueId());
        List<DominionDTO> dominions = CacheManager.instance.getCache().getDominionCache().getPlayerOwnDominionDTOs(player.getUniqueId());
        for (DominionDTO dominion : dominions) {
            groups.addAll(dominion.getGroups());
        }
        PlayerDTO playerDTO = CacheManager.instance.getPlayerCache().getPlayer(player.getUniqueId());
        if (playerDTO == null) {
            return;
        }
        Integer usingId = playerDTO.getUsingGroupTitleID();
        GroupDTO using = CacheManager.instance.getGroup(usingId);

        for (GroupDTO group : groups) {
            DominionDTO dominion = CacheManager.instance.getDominion(group.getDomID());
            if (dominion == null) {
                continue;
            }
            Line line = Line.create();
            line.append(Component.text(group.getId() + ". "));
            if (using != null && using.getId().equals(group.getId())) {
                line.append(new FunctionalButton(Language.titleListTuiText.disuseButton) {
                    @Override
                    public void function() {
                        GroupTitleCommand.useTitle(sender, "-1", args[0]);
                    }
                }.needPermission(defaultPermission).red().build());
            } else {
                line.append(new FunctionalButton(Language.titleListTuiText.useButton) {
                    @Override
                    public void function() {
                        GroupTitleCommand.useTitle(sender, group.getId().toString(), args[0]);
                    }
                }.needPermission(defaultPermission).green().build());
            }
            line.append(group.getNameColoredComponent().hoverEvent(Component.text(formatString(Language.titleListTuiText.fromDominion, dominion.getName()))));
            view.add(line);
        }

        view.showOn(player, page);
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ TUI ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ CUI ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static class TitleListCui extends ConfigurationPart {
        public String title = "§6✦ §d§lGroup Title List §6✦";
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

        public ButtonConfiguration titleItemButton = ButtonConfiguration.createMaterial(
                'i', Material.NAME_TAG, "§6✦ §f{0} §6✦",
                List.of(
                        "§7From dominion: §b{0}",
                        "",
                        "§e▶ Click to equip this title",
                        "§8  and show it to everyone!",
                        "",
                        "§7Status: §cInactive"
                )
        );

        public ButtonConfiguration activeTitleItemButton = ButtonConfiguration.createMaterial(
                'i', Material.GOLDEN_HELMET, "§6★ §e{0} §6★ §a(ACTIVE)",
                List.of(
                        "§7From dominion: §b{0}",
                        "",
                        "§a✓ This is your current title",
                        "§8  Everyone can see this!",
                        "",
                        "§c▶ Click to remove this title",
                        "",
                        "§7Status: §aActive"
                )
        );

        public ButtonConfiguration backButton = ButtonConfiguration.createMaterial(
                '<', Material.RED_STAINED_GLASS_PANE,
                "§c« Back to Main Menu",
                List.of(
                        "§7Return to the main menu",
                        "§8to access other features.",
                        "",
                        "§e▶ Click to go back"
                )
        );
    }

    @Override
    protected void showCUI(Player player, String... args) throws Exception {
        ChestListView view = ChestUserInterfaceManager.getInstance().getListViewOf(player);
        view.setTitle(ChestUserInterface.titleListCui.title);
        view.applyListConfiguration(ChestUserInterface.titleListCui.listConfiguration, toIntegrity(args[0], 1));

        List<GroupDTO> groups = CacheManager.instance.getPlayerCache().getPlayerGroupTitleList(player.getUniqueId());
        List<DominionDTO> dominions = CacheManager.instance.getCache().getDominionCache().getPlayerOwnDominionDTOs(player.getUniqueId());
        for (DominionDTO dominion : dominions) {
            groups.addAll(dominion.getGroups());
        }
        PlayerDTO playerDTO = CacheManager.instance.getPlayerCache().getPlayer(player.getUniqueId());
        if (playerDTO == null) {
            return;
        }
        Integer usingId = playerDTO.getUsingGroupTitleID();
        GroupDTO using = CacheManager.instance.getGroup(usingId);

        for (GroupDTO group : groups) {
            DominionDTO dominion = CacheManager.instance.getDominion(group.getDomID());
            if (dominion == null) {
                continue;
            }

            boolean isActive = using != null && using.getId().equals(group.getId());
            ChestButton btn = new ChestButton(isActive ? 
                    ChestUserInterface.titleListCui.activeTitleItemButton : 
                    ChestUserInterface.titleListCui.titleItemButton) {
                @Override
                public void onClick(ClickType type) {
                    if (isActive) {
                        GroupTitleCommand.useTitle(player, "-1", args[0]);
                    } else {
                        GroupTitleCommand.useTitle(player, group.getId().toString(), args[0]);
                    }
                }
            };
            btn = btn.setDisplayNameArgs(group.getNamePlain());
            btn = btn.setLoreArgs(dominion.getName());
            view = view.addItem(btn);
        }

        view.setButton(ChestUserInterface.titleListCui.backButton.getSymbol(),
                new ChestButton(ChestUserInterface.titleListCui.backButton) {
                    @Override
                    public void onClick(ClickType type) {
                        MainMenu.show(player, "1");
                    }
                }
        );

        view.open();
    }

}
