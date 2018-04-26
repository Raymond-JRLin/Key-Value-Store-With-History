/**
 * Friend class to record users' friends info
 */
public class Friend {

    private String name;
    private int createdTime;
    private int ope;

    /**
     * Constructor
     *
     * @param createdTime: timestamp of operating this Friend
     * @param ope: operation with -1 of deletion and 1 of put
     * @param name: friend's name
     */
    public Friend(int createdTime, int ope, String name) {
        this.name = name;
        this.createdTime = createdTime;
        this.ope = ope; // -1 means delete, 1 means add(put)
    }

    /**
     * method to get this friend's name
     *
     * @return friend's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * method to get the operation on this friend
     *
     * @return operation in int format
     */
    public int getOpe() {
        return this.ope;
    }

    @Override
    public String toString() {
        String operation = this.ope == 1 ? "put" : "del";
        return operation + " " + name + " " + String.valueOf(createdTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Friend friend = (Friend) obj;
        return this.name.equals(friend.name);
    }

    @Override
    public int hashCode() {
        final int SEED = 33;
        final int MOD = Integer.MAX_VALUE;
        int hash = 1;
        for (char c : this.name.toCharArray()) {
            hash = (hash * SEED + c - 'a') % MOD;
        }
        return hash;
    }
}
