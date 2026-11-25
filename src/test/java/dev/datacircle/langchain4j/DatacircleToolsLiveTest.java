package dev.datacircle.langchain4j;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Each tool once on the live API, through LangChain4j's own tool executor. Run with DATACIRCLE_LIVE=1. */
@EnabledIfEnvironmentVariable(named = "DATACIRCLE_LIVE", matches = "1")
class DatacircleToolsLiveTest {

    private final Map<String, DefaultToolExecutor> tools = new HashMap<>();

    DatacircleToolsLiveTest() {
        DatacircleTools datacircle = new DatacircleTools();
        for (Method method : DatacircleTools.class.getMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                tools.put(method.getAnnotation(Tool.class).name(), new DefaultToolExecutor(datacircle, method));
            }
        }
    }

    private String call(String name, String arguments) {
        ToolExecutionRequest request =
                ToolExecutionRequest.builder().id("1").name(name).arguments(arguments).build();
        return tools.get(name).execute(request, "default");
    }

    @Test
    void listsTheApis() {
        String answer = call("datacircle_list_apis", "{\"query\": \"email\"}");
        System.out.println(answer);
        assertTrue(answer.contains("(email-finder): "), answer);
        assertTrue(answer.contains(" APIs. Pass a slug to datacircle_best_plans"), answer);
    }

    @Test
    void givesTheBestPlans() {
        String answer = call("datacircle_best_plans", "{\"api\": \"email-finder\", \"limit\": 3}");
        System.out.println(answer);
        assertTrue(answer.startsWith("Employee email (email-finder): the best 3 of "), answer);
        assertTrue(answer.contains("\n1. ") && answer.contains("\n3. "), answer);
    }

    @Test
    void searchesThePlans() {
        String answer = call("datacircle_search_plans", "{\"query\": \"hunter\", \"sort\": \"price\", \"limit\": 3}");
        System.out.println(answer);
        assertTrue(answer.startsWith("Found ") && answer.contains("; showing 1 to 3."), answer);
        assertTrue(answer.contains("- Hunter, "), answer);
    }

    @Test
    void givesAProvidersRanks() {
        String answer = call("datacircle_provider", "{\"provider\": \"Hunter\"}");
        System.out.println(answer);
        assertTrue(answer.startsWith("Hunter (https://hunter.io) sells "), answer);
        assertTrue(answer.contains(": rank "), answer);
    }
}
