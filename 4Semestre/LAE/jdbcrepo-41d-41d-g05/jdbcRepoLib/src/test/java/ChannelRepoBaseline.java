import kotlin.jvm.internal.Reflection;
import org.jetbrains.annotations.NotNull;
import pt.isel.RepositoryReflect;
import pt.isel.chat.Channel;
import pt.isel.chat.ChannelType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelRepoBaseline extends RepositoryReflect <String, Channel> {
    public ChannelRepoBaseline(Connection var1) {
        super(var1, Reflection.getOrCreateKotlinClass(Channel.class));
    }

    @NotNull
    @Override
    public Channel mapResultSetToObjects(@NotNull ResultSet rs) {
        try {
            return new Channel(
                    rs.getString("name"),
                    ChannelType.valueOf(rs.getString("type")),
                    rs.getLong("created_at"),
                    rs.getBoolean("is_archived"),
                    rs.getInt("max_message_length"),
                    rs.getInt("max_members"),
                    rs.getBoolean("is_read_only"),
                    rs.getLong("last_message_timestamp")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet to Channel", e);
        }
    }
}