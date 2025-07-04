package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Review extends JsonEntity {
    private final LocalDate timestamp;
    private final Restaurant restaurant;
    private final Person person;

    private int value;
    private String text;
    private String reply;

    public Review(Restaurant restaurant, Person person, int value, LocalDate timestamp, String text) {
        super("reviews");
        this.timestamp = timestamp;
        this.restaurant = restaurant;
        this.person = person;

        this.value = value;
        this.text = text;

        build();
    }

    public Review(Restaurant restaurant, Person person, int value, String text) {
        super("reviews");
        this.restaurant = restaurant;
        this.person = person;
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
    public boolean save() {
        return restaurant.save();
    }

    @Override
    public String toString() {
        return "Review{" +
                "timestamp=" + timestamp +
                ", company=" + restaurant +
                ", person=" + person +
                ", value=" + value +
                ", text='" + text + '\'' +
                ", reply='" + reply + '\'' +
                ", id=" + id +
                '}';
    }
}
