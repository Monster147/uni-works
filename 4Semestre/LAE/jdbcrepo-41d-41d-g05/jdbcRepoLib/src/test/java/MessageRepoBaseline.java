import kotlin.jvm.internal.Reflection;
import org.jetbrains.annotations.NotNull;
import pt.isel.RepositoryReflect;
import pt.isel.chat.Channel;
import pt.isel.chat.Message;
import pt.isel.chat.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageRepoBaseline extends RepositoryReflect <Long, Message> {
    public RepositoryReflect<Long, User> UserRepo;
    public RepositoryReflect<String, Channel> ChannelRepo;

    public MessageRepoBaseline(Connection var1) {
        super(var1, Reflection.getOrCreateKotlinClass(Message.class));
        this.UserRepo = new RepositoryReflect<Long, User>(var1, Reflection.getOrCreateKotlinClass(User.class));
        this.ChannelRepo = new RepositoryReflect<String, Channel>(var1, Reflection.getOrCreateKotlinClass(Channel.class));
    }

    @NotNull
    @Override
    public Message mapResultSetToObjects(@NotNull ResultSet var1) {
        try {
            return new Message(var1.getLong("id"), var1.getString("content"), var1.getLong("timestamp"), (User)this.UserRepo.getById(var1.getLong("user_id")), (Channel)this.ChannelRepo.getById(var1.getString("channel_name")));
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet to User", e);
        }
    }
}
