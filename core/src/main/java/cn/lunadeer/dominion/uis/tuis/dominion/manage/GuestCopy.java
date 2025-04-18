package cn.lunadeer.dominion.uis.tuis.dominion.manage;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.cache.CacheManager;
import cn.lunadeer.dominion.commands.CopyCommand;
import cn.lunadeer.dominion.configuration.Language;
import cn.lunadeer.dominion.utils.Notification;
import cn.lunadeer.dominion.utils.configuration.ConfigurationPart;
import cn.lunadeer.dominion.utils.stui.ListView;
import cn.lunadeer.dominion.utils.stui.components.Line;
import cn.lunadeer.dominion.utils.stui.components.buttons.FunctionalButton;
import cn.lunadeer.dominion.utils.stui.components.buttons.ListViewButton;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static cn.lunadeer.dominion.Dominion.defaultPermission;
import static cn.lunadeer.dominion.misc.Asserts.assertDominionAdmin;
import static cn.lunadeer.dominion.misc.Converts.*;

public class GuestCopy {

    public static class GuestCopyTuiText extends ConfigurationPart {
        public String title = "Select Dominion to Copy From";
        public String button = "COPY FROM";
        public String description = "Copy Guest Settings From Other Dominion.";
        public String back = "BACK";
    }

    public static ListViewButton button(CommandSender sender, String toDominionName) {
        return (ListViewButton) new ListViewButton(Language.guestCopyTuiText.button) {
            @Override
            public void function(String pageStr) {
                show(sender, toDominionName, pageStr);
            }
        }.needPermission(defaultPermission).setHoverText(Language.guestCopyTuiText.description);
    }

    public static void show(CommandSender sender, String toDominionName, String pageStr) {
        try {
            DominionDTO dominion = toDominionDTO(toDominionName);
            Player player = toPlayer(sender);
            assertDominionAdmin(sender, dominion);
            int page = toIntegrity(pageStr);

            ListView view = ListView.create(10, button(sender, toDominionName));
            view.title(Language.guestCopyTuiText.title).navigator(Line.create()
                    .append(GuestSetting.button(sender, toDominionName).setText(Language.guestCopyTuiText.back).build()));
            List<DominionDTO> dominions = CacheManager.instance.getPlayerOwnDominionDTOs(player.getUniqueId());
            for (DominionDTO fromDominion : dominions) {
                if (fromDominion.getId().equals(dominion.getId())) continue;
                view.add(Line.create()
                        .append(new FunctionalButton(Language.guestCopyTuiText.button) {
                            @Override
                            public void function() {
                                CopyCommand.copyGuest(sender, fromDominion.getName(), toDominionName);
                            }
                        }.needPermission(defaultPermission).build())
                        .append(Component.text(fromDominion.getName()))
                );
            }
            view.showOn(sender, page);
        } catch (Exception e) {
            Notification.error(sender, e.getMessage());
        }
    }
}
