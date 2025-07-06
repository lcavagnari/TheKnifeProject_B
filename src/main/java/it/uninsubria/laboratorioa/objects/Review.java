package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Review extends JsonEntity {
    private final LocalDate timestamp;
    private final Restaurant restaurant;
    private final User user;

    private int value;
    private String text;
    private String reply;

    public Review(Restaurant restaurant, User user, int value, LocalDate timestamp, String text) {
        super(UUID.randomUUID());
        this.timestamp = timestamp;
        this.restaurant = restaurant;
        this.user = user;

        this.value = value;
        this.text = text;

        build();
    }

    public Review(Restaurant restaurant, User user, int value, String text) {
        super("reviews");
        this.restaurant = restaurant;
        this.user = user;
        this.value = value;
        this.text = text;

        this.timestamp = LocalDate.now();
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
        this.jsonObject.put("timestamp", Constants.TIMESTAMP_FORMAT.format(timestamp))
                .put("user", "")
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
