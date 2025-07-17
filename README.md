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

    public class PersonalTaskManagerViolations {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Phương thức trợ giúp để tải dữ liệu (sẽ được gọi lặp lại)
    private static JSONArray loadTasksFromDb() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
        }
        return new JSONArray();
    }

    // Phương thức trợ giúp để lưu dữ liệu
    private static void saveTasksToDb(JSONArray tasksData) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasksData.toJSONString());
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }

    private boolean isTitleValid(String title) {
        return title != null && !title.trim().isEmpty();
    }
    private boolean isPriorityValid(String priority) {
        return List.of("Thấp", "Trung bình", "Cao").contains(priority);
    }
    private LocalDate parseDueDate(String dueDateStr) {
        try {
            return LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }


    private boolean isDuplicate(JSONArray tasks, String title, String dueDate) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (title.equalsIgnoreCase((String) task.get("title")) &&
                dueDate.equals(task.get("due_date"))) {
                return true;
            }
        }
        return false;
    }

    private JSONObject buildTask(String title, String description, String dueDateStr, String priority) {
        JSONObject task = new JSONObject();
        String taskId = UUID.randomUUID().toString();

        task.put("id", taskId);
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dueDateStr);
        task.put("priority", priority);
        task.put("status", "Chưa hoàn thành");
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        task.put("created_at", now);
        task.put("last_updated_at", now);

        return task;
    }
    public JSONObject addNewTask(String title, String description, String dueDateStr, String priority) {
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
        if (isDuplicate(tasks, title, dueDateStr)) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại với cùng tiêu đề và ngày đến hạn.");
            return null;
        }

  
        // Kiểm tra trùng lặp
        if (isDuplicate(tasks, title, dueDateStr)) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại với cùng tiêu đề và ngày đến hạn.");
            return null;
        }

        saveTasksToDb(tasks);

        System.out.println("Đã thêm nhiệm vụ thành công với ID: " + task.get("id"));
        return task;
    }

    public static void main(String[] args) {
        PersonalTaskManagerViolations manager = new PersonalTaskManagerViolations();
         
        System.out.println("\nThêm nhiệm vụ hợp lệ:");
        manager.addNewTask(
            "Mua sách",
            "Sách Công nghệ phần mềm.",
            "2025-07-20",
            "Cao",

        );

        System.out.println("\nThêm nhiệm vụ trùng lặp (minh họa DRY - lặp lại code đọc/ghi DB và kiểm tra trùng):");
        manager.addNewTask(
            "Mua sách",
            "Sách Công nghệ phần mềm.",
            "2025-07-20",
            "Cao",

        );

        System.out.println("\nThêm nhiệm vụ lặp lại (minh họa YAGNI - thêm tính năng không cần thiết ngay):");
        manager.addNewTask(
            "Tập thể dục",
            "Tập gym 1 tiếng.",
            "2025-07-21",
            "Trung bình",

        );

        System.out.println("\nThêm nhiệm vụ với tiêu đề rỗng:");
        manager.addNewTask(
            "",
            "Nhiệm vụ không có tiêu đề.",
            "2025-07-22",
            "Thấp",

        );
    }

# NMCNPM
