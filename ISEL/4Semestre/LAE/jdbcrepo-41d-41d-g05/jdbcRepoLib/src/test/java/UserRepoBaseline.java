import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import pt.isel.RepositoryReflect;
import java.sql.SQLException;
import pt.isel.chat.User;


public class UserRepoBaseline extends RepositoryReflect <Long, User> {
    public UserRepoBaseline(Connection var1, KClass<User> kclass ) {
        super(var1, kclass);
    }

    @NotNull
    @Override
    public User mapResultSetToObjects(@NotNull ResultSet rs) {
        try {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            String email = rs.getString("email");
            Date birthdate = rs.getDate("birthdate");
            return new User(id, name, email, birthdate);
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet to User", e);
        }
    }
}
