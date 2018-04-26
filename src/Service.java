import java.util.*;

/**
 * APIs for Key Value Store
 */
public class Service {
    public int timestamp; // record the timestamp
    public Map<String, TreeMap<Integer, Friend>> users; // record all historic users info, TreeMap sorts friends names with timestamp order: <userName, <timestamp, friendName>>
    public Map<String, TreeMap<Integer,List<Friend>>> deleted; // record all deleted friends associated with users: <userName, <timestamp, {friendName}>>
    public Map<String, List<Friend>> currFriends; // record only users' current friends info before deletion or new friends after deletion: <userName, {friendName}>

    /**
     * Constructor
     */
    public Service() {
        // initialization
        timestamp = 0; // I assume the number of operations will be in int range, and each valid operation will be on 1 single timestamp
        users = new HashMap<>();
        deleted = new HashMap<>();
        currFriends = new HashMap<>();
    }

    // some codes used to mark result status
    private final int NOUSER = -1; // marks that there's is no such user
    private final int HASDELETED = 0; // the user has been deleted
    private final int SUCCESS = 1; // delete successfully
    private final int NOFRIEND = 2; // no such searching friend
    private final int INVALIDTIME = 3; // invalid time error

    /**
     * realize put(key, value) method
     *
     * @param userName: current user - key
     * @param friendName: a new user's friend - value
     */
    public void put(String userName, String friendName) {
        // 1. create this new friend
        Friend friend = new Friend(timestamp, 1, friendName);

        // 2. insert this new friend into user's friend map with timestamp
        TreeMap<Integer, Friend> friends = users.getOrDefault(userName, new TreeMap<>());
        friends.put(timestamp, friend);
        users.put(userName, friends);

        // 3. insert this new friend into current friend map if necessary
        List<Friend> list = currFriends.getOrDefault(userName, new LinkedList<>());
        // check if user already has this friend in current friend list
        boolean alreadyHas = false;
        for (Friend f :
                list) {
            if (f.equals(friend)) {
                alreadyHas = true;
                break;
            }
        }
        // if user does not have this friend in current friend list, then add it
        if (!alreadyHas) {
            list.add(friend); // add friend to user's list
            currFriends.put(userName, list); // update currFriends map
        }

        // update timestamp
        timestamp++;
    }

    /**
     * realize get(key) method
     *
     * @param userName: the user been searched
     * @return a list of friends' names
     */
    public ResultType get(String userName) {
        timestamp++; // update timestamp

        // 1. edge case: search a user which is not in our store yet
        if (!users.containsKey(userName)) {
            return new ResultType(NOUSER);
        }

        // 2. get current friends list
        List<Friend> friends = new LinkedList<>(currFriends.get(userName));
        if (friends.isEmpty()) {
            return new ResultType(NOFRIEND);
        }

        // 3. get each friend's name
        List<String> result = new LinkedList<>();
        for (Friend friend :
                friends) {
            result.add(friend.getName());
        }

        // 4. return resultType
        ResultType resultType = new ResultType(SUCCESS);
        resultType.setResult(result);
        return resultType;
    }

    /**
     * realize get(key, value) method
     *
     * @param userName: the user who does get operation
     * @param time: a specific timestamp
     * @return a list of friends' names before time of this user
     */
    public ResultType get(String userName, int time) {
        // 1. edge case: input time is larger than timestamp
        if (time >= timestamp) {
            return new ResultType(INVALIDTIME);
        }

        // 2. edge case: this user is not in our store yet
        if (!users.containsKey(userName)) {
            return new ResultType(NOUSER);
        }

        timestamp++; // update timestamp

        List<Friend> friends = new LinkedList<>(); // friends list

        // 3. iterate friends with timestamp up to time
        for (Map.Entry<Integer, Friend> entry : users.get(userName).subMap(0, time + 1).entrySet()) {
            friends.add(entry.getValue());
        }

        // 4. remove friends who may be deleted by user during this time
        if (deleted.containsKey(userName)) {
            Map<Integer, List<Friend>> deletedFriends = deleted.get(userName).subMap(0, time + 1);
            // if some friends are indeed deleted during this time
            if (!deletedFriends.isEmpty()) {
                for (Map.Entry<Integer, List<Friend>> entry :
                        deletedFriends.entrySet()) {
                    // get each deleted friend in different timestamp
                    List<Friend> list = entry.getValue();
                    for (Friend friend :
                            list) {
                        friends.remove(friend); // remove deleted friend
                    }
                }
            }
        }

        // 5. get final return result with friends' names
        List<String> result = new LinkedList<>();
        for (Friend friend :
                friends) {
            result.add(friend.getName());
        }

        // 6. return resultType
        ResultType resultType = new ResultType(SUCCESS);
        resultType.setResult(result);
        return resultType;
    }

    /**
     * realize delete(key) method
     *
     * @param userName: the user which is gonna delete
     * @return true means deletion completed, false means there's no such user in our store
     */
    public int delete(String userName) {
        // 1. edge case: this user is not in our store yet
        if (!users.containsKey(userName)) {
            timestamp++; // update timestamp
            return NOUSER;
        }

        // 2. edge case: this user does not have current friends list
        if (!currFriends.containsKey(userName)) {
            timestamp++; // update timestamp
            return HASDELETED;
        }

        // 3. user has some friends right now, record this operation and deleted friends in deleted map
        List<Friend> friends = new LinkedList<>(currFriends.get(userName)); // get friends list to be deleted
        TreeMap<Integer, List<Friend>> toDel = deleted.getOrDefault(userName, new TreeMap<>()); // this user's deleted map
        List<Friend> toDelFriends = new LinkedList<>(); // list to record deleted friends
        for (Friend friend :
                friends) {
            toDelFriends.add(new Friend(timestamp, -1, friend.getName())); // added deleted friends
        }
        toDel.put(timestamp, toDelFriends); // add deleted list with timestamp
        deleted.put(userName, toDel); // update deleted map

        // 4. change this user's current friend from current friends lis, keep historic data in users map
        currFriends.remove(userName); // remove from current friends list
        timestamp++; // update timestamp

        return SUCCESS;
    }

    /**
     * realize delete(key, value) method: delete a specific friend of a user
     *
     * @param userName: the user who wants do deletion
     * @param friendName: the friend who is gonna be deleted
     * @return true means deletion completed, false means this friend is not user's friend yet
     */
    public int delete(String userName, String friendName) {
        // 1. edge case: this user is not in our store yet
        if (!users.containsKey(userName)) {
            timestamp++; // update timestamp
            return NOUSER;
        }

        // 2. user has no friends yet, or friends have been all deleted already
        if (!currFriends.containsKey(userName)) {
            timestamp++; // update timestamp
            return HASDELETED;
        }

        // 3. delete a specific friend in current friend list if it presents, keep historic data in users map
        List<Friend> friends = currFriends.get(userName);
        if (friends.isEmpty()) {
            timestamp++; // update timestamp
            return HASDELETED;
        }
        Friend deletedFri = new Friend(timestamp, -1, friendName); // the friend to be deleted
        boolean doDeletion = friends.remove(deletedFri); // remove this friend from user's current friend list if it presents, mark if it's done

        // 4. add deleted friend info into deleted map
        if (doDeletion) {
            currFriends.put(userName, friends); // update current friends list
            TreeMap<Integer, List<Friend>> toDel = deleted.getOrDefault(userName, new TreeMap<>());
            List<Friend> toDelFriend = new LinkedList<>();
            toDelFriend.add(deletedFri); // add this deleted friends
            toDel.put(timestamp, toDelFriend); // update this user's deleted friend list
            deleted.put(userName, toDel); // update deleted
            timestamp++; // update timestamp
            return SUCCESS;
        } else {
            timestamp++; // update timestamp
            return HASDELETED;
        }
    }

    /**
     * realize diff(key, time1, time2) method
     *
     * @param userName: the user who does diff operation
     * @param time1: the first checking time
     * @param time2: the second checking time (time1 < time2 guaranteed)
     * @return a list of friends' names which are the difference between time1 and time2
     */
    public ResultType diff(String userName, int time1, int time2) {
        // 1. edge case: this user is not in our store yet, or time is larger
        ResultType resultType1 = get(userName, time1);
        ResultType resultType2 = get(userName, time2);
        // we can only check time1, since we make sure time2 > time2 when call this method
        if (resultType1.code != SUCCESS) {
            return new ResultType(resultType1.code);
        }
        if (resultType2.code != SUCCESS) {
            return new ResultType(resultType2.code);
        }

        timestamp--; // since we call twice get(), so minus one

        // 2. find historic info associate 2 time spot
        List<String> friends1 = resultType1.getResult(); // friend name list of this user at time1
        List<String> friends2 = resultType2.getResult(); // friend name list of this user at time2
        Set<String> set = new HashSet<>(); // set to remove same friends
        // record all friends at time1
        for (String fri1 :
                friends1) {
            set.add(fri1); // record info in set
        }
        // remove duplicates and record difference
        for (String fri2 :
                friends2) {
            if (set.contains(fri2)) {
                // duplicates
                set.remove(fri2);
                friends1.remove(fri2);
            } else {
                // different friends
                set.add(fri2);
                friends1.add(fri2);
            }
        }

        // 3. return
        ResultType resultType = new ResultType(SUCCESS);
        resultType.setResult(friends1);
        return resultType;
    }
}