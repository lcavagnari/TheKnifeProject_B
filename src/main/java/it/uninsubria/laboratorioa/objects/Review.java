package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.users.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Review extends JsonEntity {
    private final LocalDateTime timestamp;
    private final Restaurant restaurant;
    private final User user;

    private int value;
    private String text;
    private String reply;

    public Review(Restaurant restaurant, User user, int value, LocalDateTime timestamp, String text, String reply) {
        super(UUID.randomUUID());
        this.timestamp = timestamp;
        this.restaurant = restaurant;
        this.user = user;

        this.value = value;
        this.text = text;
        this.reply = reply;

        build();
    }

    public Review(Restaurant restaurant, User user, int value, LocalDateTime timestamp, String text) {
        super(UUID.randomUUID());
        this.timestamp = timestamp;
        this.restaurant = restaurant;
        this.user = user;

        this.value = value;
        this.text = text;
        this.reply = null;

        build();
    }

    public Review(Restaurant restaurant, User user, int value, String text) {
        this.restaurant = restaurant;
        this.user = user;
        this.value = value;
        this.text = text;

        this.timestamp = LocalDateTime.now();
    }

    public void setValue(int value) {
        if (value < 1 || value > 5) return;

        this.value = value;
        rebuild();
    }

    public void setText(String text) {
        if (text == null || text.isBlank()) return;

        this.text = text;
        rebuild();
    }

    public void setReply(String reply) {
        if (text == null || text.isBlank()) return;

        this.reply = reply;
        rebuild();
    }

    @Override
    protected void build() {
        this.jsonObject.put("timestamp", timestamp.toString())
                .put("user", user.getId().toString())
                .put("value", value)
                .put("text", text)
                .put("reply", reply);
    }

    @Override
    public String toString() {
        return "Review{" +
                "timestamp=" + timestamp +
                ", company=" + restaurant +
                ", person=" + user +
                ", value=" + value +
                ", text='" + text + '\'' +
                ", reply='" + reply + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean save() {
        build();
        return false;
    }
}
