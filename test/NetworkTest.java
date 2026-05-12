import com.oocourse.spec1.main.UserInterface;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    private Network network;

    @Before
    public void setUp() {
        network = new Network();
    }

    @Test
    public void none() throws Exception {
        assertEquals(0,network.queryMutualFollowingSum());
    }

    @Test
    public void oneUser() throws Exception {
        network.addUser(1,"nihao",20);
        assertEquals(0,network.queryMutualFollowingSum());
    }

    @Test
    public void twoUsers() throws Exception {
        network.addUser(1,"nihao",20);
        network.addUser(2,"nike",20);
        network.followUser(1,2);
        network.followUser(2,1);
        assertEquals(1,network.queryMutualFollowingSum());
    }

    @Test
    public void mulitUser() throws Exception {
        network.addUser(1,"nihao",20);
        network.addUser(2,"nihao2",20);
        network.addUser(3,"nihao3",20);
        network.addUser(4,"nihao4",20);
        network.followUser(1,2);
        network.followUser(2,1);
        network.followUser(3,2);
        network.followUser(4,2);
        assertEquals(1,network.queryMutualFollowingSum());
    }

    @Test
    public void pure()  throws Exception {
        network.addUser(1,"nihao",20);
        network.addUser(2,"nihao2",20);
        network.addUser(3,"nihao3",20);

        Network network2 = new Network();
        network2.addUser(1,"nihao",20);
        network2.addUser(2,"nihao2",20);
        network2.addUser(3,"nihao3",20);

        network2.followUser(1,2);
        network2.followUser(2,1);
        network2.followUser(3,2);

        network.followUser(1,2);
        network.followUser(3,2);
        network.followUser(2,1);

        UserInterface[] before = network2.getUsers();

        assertEquals(1,network.queryMutualFollowingSum());

        UserInterface[] after = network.getUsers();

        for(int i = 0; i < before.length; i++) {
            boolean isEqual = ((User)before[i]).strictEquals(after[i]);
            assertTrue(isEqual);
        }

    }

}
