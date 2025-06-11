package cn.lunadeer.dominion.commands;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.flag.EnvFlag;
import cn.lunadeer.dominion.api.dtos.flag.PriFlag;
import cn.lunadeer.dominion.cache.CacheManager;
import cn.lunadeer.dominion.configuration.Language;
import cn.lunadeer.dominion.events.group.GroupAddMemberEvent;
import cn.lunadeer.dominion.events.group.GroupCreateEvent;
import cn.lunadeer.dominion.events.group.GroupSetFlagEvent;
import cn.lunadeer.dominion.events.member.MemberAddedEvent;
import cn.lunadeer.dominion.events.member.MemberSetFlagEvent;
import cn.lunadeer.dominion.misc.CommandArguments;
import cn.lunadeer.dominion.uis.tuis.dominion.manage.EnvSetting;
import cn.lunadeer.dominion.uis.tuis.dominion.manage.GuestSetting;
import cn.lunadeer.dominion.uis.tuis.dominion.manage.group.GroupList;
import cn.lunadeer.dominion.uis.tuis.dominion.manage.member.MemberList;
import cn.lunadeer.dominion.utils.Notification;
import cn.lunadeer.dominion.utils.command.SecondaryCommand;
import cn.lunadeer.dominion.utils.configuration.ConfigurationPart;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cn.lunadeer.dominion.api.DominionAPI.defaultPermission;
import static cn.lunadeer.dominion.misc.Converts.toDominionDTO;

public class CopyCommand {

    public static class CopyCommandText extends ConfigurationPart {
        public String copyEnvSuccess = "Copied environment flag from {0} to {1} success.";
        public String copyGuestSuccess = "Copied guest privilege flag from {0} to {1} success.";
        public String copyMemberSuccess = "Copied members from {0} to {1} success.";
        public String copyGroupSuccess = "Copied groups from {0} to {1} success.";
    }

    public static void copyEnvironment(CommandSender sender, String from, String to) {
        try {
            DominionDTO fromDominion = toDominionDTO(from);
            DominionDTO toDominion = toDominionDTO(to);
            for (EnvFlag flag : fromDominion.getEnvironmentFlagValue().keySet()) {
                if (toDominion.getEnvFlagValue(flag) == fromDominion.getEnvFlagValue(flag)) continue;
                toDominion.setEnvFlagValue(flag, fromDominion.getEnvFlagValue(flag));
            }
            Notification.info(sender, Language.copyCommandText.copyEnvSuccess, fromDominion.getName(), toDominion.getName());
            EnvSetting.show(sender, to, "1");
        } catch (Exception e) {
            Notification.error(sender, e);
        }
    }

    public static SecondaryCommand copyEnvironmentCommand = new SecondaryCommand("copy_env", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.RequiredDominionArgument()
    )) {
        @Override
        public void executeHandler(CommandSender sender) {
            copyEnvironment(sender, getArgumentValue(0), getArgumentValue(1));
        }
    }.needPermission(defaultPermission).register();

    public static void copyGuest(CommandSender sender, String from, String to) {
        try {
            DominionDTO fromDominion = toDominionDTO(from);
            DominionDTO toDominion = toDominionDTO(to);
            for (PriFlag flag : fromDominion.getGuestPrivilegeFlagValue().keySet()) {
                if (toDominion.getGuestFlagValue(flag) == fromDominion.getGuestFlagValue(flag)) continue;
                toDominion.setGuestFlagValue(flag, fromDominion.getGuestFlagValue(flag));
            }
            Notification.info(sender, Language.copyCommandText.copyGuestSuccess, fromDominion.getName(), toDominion.getName());
            GuestSetting.show(sender, to, "1");
        } catch (Exception e) {
            Notification.error(sender, e);
        }
    }

    public static SecondaryCommand copyGuestCommand = new SecondaryCommand("copy_guest", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.RequiredDominionArgument()
    )) {
        @Override
        public void executeHandler(CommandSender sender) {
            copyGuest(sender, getArgumentValue(0), getArgumentValue(1));
        }
    }.needPermission(defaultPermission).register();

    public static void copyMember(CommandSender sender, String from, String to) {
        try {
            DominionDTO fromDominion = toDominionDTO(from);
            DominionDTO toDominion = toDominionDTO(to);
            for (MemberDTO member : fromDominion.getMembers()) {
                try {
                    MemberDTO toMember = CacheManager.instance.getMember(toDominion, member.getPlayerUUID());
                    if (toMember == null) {
                        MemberAddedEvent event = new MemberAddedEvent(sender, toDominion, member.getPlayer());
                        event.call();
                        if (event.isCancelled()) continue;
                        if (event.getMember() == null) continue;
                        toMember = event.getMember();
                    }
                    for (PriFlag flag : member.getFlagsValue().keySet()) {
                        if (toMember.getFlagValue(flag) == member.getFlagValue(flag)) continue;
                        new MemberSetFlagEvent(sender,
                                toDominion,
                                toMember,
                                flag,
                                member.getFlagValue(flag)).call();
                    }
                } catch (Exception e) {
                    Notification.warn(sender, e.getMessage());
                }
            }
            Notification.info(sender, Language.copyCommandText.copyMemberSuccess, fromDominion.getName(), toDominion.getName());
            MemberList.show(sender, to, "1");
        } catch (Exception e) {
            Notification.error(sender, e);
        }
    }

    public static SecondaryCommand copyMemberCommand = new SecondaryCommand("copy_member", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.RequiredDominionArgument()
    )) {
        @Override
        public void executeHandler(CommandSender sender) {
            copyMember(sender, getArgumentValue(0), getArgumentValue(1));
        }
    }.needPermission(defaultPermission).register();

    public static void copyGroup(CommandSender sender, String from, String to) {
        try {
            copyMember(sender, from, to); // copy member first
            DominionDTO fromDominion = toDominionDTO(from);
            DominionDTO toDominion = toDominionDTO(to);
            for (GroupDTO group : fromDominion.getGroups()) {
                try {
                    GroupDTO toGroup = toDominion.getGroups().stream()
                            .filter(g -> g.getNamePlain().equals(group.getNamePlain()))
                            .findFirst()
                            .orElse(null);
                    if (toGroup == null) {
                        // create group in target dominion
                        GroupCreateEvent event = new GroupCreateEvent(sender, toDominion, group.getNameRaw());
                        event.call();
                        if (event.isCancelled()) continue;
                        if (event.getGroup() == null) continue;
                        toGroup = event.getGroup();
                    }
                    // set group flags
                    for (PriFlag flag : group.getFlagsValue().keySet()) {
                        if (toGroup.getFlagValue(flag) == group.getFlagValue(flag)) continue;
                        new GroupSetFlagEvent(sender,
                                toDominion,
                                toGroup,
                                flag,
                                group.getFlagValue(flag)).call();
                    }
                    // set group members
                    for (MemberDTO fromMember : fromDominion.getMembers()) {
                        MemberDTO toMember = CacheManager.instance.getMember(toDominion, fromMember.getPlayerUUID());
                        if (toMember == null) continue;
                        if (toMember.getGroupId().equals(toGroup.getId())) continue;
                        new GroupAddMemberEvent(sender, toDominion, toGroup, toMember).call();
                    }
                } catch (Exception e) {
                    Notification.warn(sender, e.getMessage());
                }
            }
            Notification.info(sender, Language.copyCommandText.copyGroupSuccess, fromDominion.getName(), toDominion.getName());
            GroupList.show(sender, to, "1");
        } catch (Exception e) {
            Notification.error(sender, e);
        }
    }

    public static SecondaryCommand copyGroupCommand = new SecondaryCommand("copy_group", List.of(
            new CommandArguments.RequiredDominionArgument(),
            new CommandArguments.RequiredDominionArgument()
    )) {
        @Override
        public void executeHandler(CommandSender sender) {
            copyGroup(sender, getArgumentValue(0), getArgumentValue(1));
        }
    }.needPermission(defaultPermission).register();
}
