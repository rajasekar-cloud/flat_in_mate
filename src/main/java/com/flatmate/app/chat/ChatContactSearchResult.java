package com.flatmate.app.chat;

public class ChatContactSearchResult {

    private String matchId;
    private String userId;
    private String name;
    private String firstName;
    private String lastName;
    private String profilePic;
    private String lastMessage;
    private String lastMessageAt;

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(String lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
