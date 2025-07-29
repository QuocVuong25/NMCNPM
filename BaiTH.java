import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

    public class PersonalTaskManager {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> VALID_PRIORITIES = List.of("Thấp", "Trung bình", "Cao");

    public JSONObject addNewTask(String title, String description, String dueDateStr, String priority) {
        // Kiểm tra hợp lệ
        if (!isTitleValid(title)) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return null;
        }
        LocalDate dueDate = parseDueDate(dueDateStr);
        if (dueDate == null) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ. Định dạng phải là YYYY-MM-DD.");
            return null;
        }
        if (!isPriorityValid(priority)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Chọn Thấp, Trung bình hoặc Cao.");
            return null;
        }

        // Tải dữ liệu
        JSONArray tasks = loadTasksFromDb();

        // Kiểm tra trùng lặp
        if (isDuplicate(tasks, title, dueDateStr)) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại với cùng tiêu đề và ngày đến hạn.");
            return null;
        }

        // Tạo task và thêm
        JSONObject task = buildTask(title, description, dueDateStr, priority);
        tasks.add(task);
        saveTasksToDb(tasks);

        System.out.println("Đã thêm nhiệm vụ thành công với ID: " + task.get("id"));
        return task;
    }

    private boolean isTitleValid(String title) {
        return title != null && !title.trim().isEmpty();
    }

    private boolean isPriorityValid(String priority) {
        return VALID_PRIORITIES.contains(priority);
    }

    private LocalDate parseDueDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isDuplicate(JSONArray tasks, String title, String dueDateStr) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (title.equalsIgnoreCase((String) task.get("title")) &&
                dueDateStr.equals(task.get("due_date"))) {
                return true;
            }
        }
        return false;
    }

    private JSONObject buildTask(String title, String description, String dueDateStr, String priority) {
        JSONObject task = new JSONObject();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        task.put("id", UUID.randomUUID().toString());
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dueDateStr);
        task.put("priority", priority);
        task.put("status", "Chưa hoàn thành");
        task.put("created_at", now);
        task.put("last_updated_at", now);
        return task;
    }

    private JSONArray loadTasksFromDb() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = new JSONParser().parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
        }
        return new JSONArray();
    }

    private void saveTasksToDb(JSONArray tasks) {
        try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
            writer.write(tasks.toJSONString());
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();

        System.out.println("\nThêm nhiệm vụ hợp lệ:");
        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao");

        System.out.println("\nThêm nhiệm vụ trùng lặp:");
        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao");

        System.out.println("\nThêm nhiệm vụ ưu tiên trung bình:");
        manager.addNewTask("Tập thể dục", "Tập gym 1 tiếng.", "2025-07-21", "Trung bình");

        System.out.println("\nThêm nhiệm vụ không có tiêu đề:");
        manager.addNewTask("", "Không có tiêu đề.", "2025-07-22", "Thấp");
    }
}
