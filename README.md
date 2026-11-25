# Datacircle tools for LangChain4j

Find the best GTM APIs. We benchmark all the GTM APIs and we put the results on our website.

Our website: https://datacircle.dev

This library gives [LangChain4j](https://github.com/langchain4j/langchain4j) four tools that call Datacircle's open API, https://api.datacircle.dev/v1. You pass them to an AI service or an agent, beside your app's own tools. You need no key and no account.

| Tool | What the tool gives | Call |
| --- | --- | --- |
| `datacircle_list_apis` | Every GTM API on Datacircle, with the API's slug, number of plans, number of providers and best plan | `GET /apis` |
| `datacircle_best_plans` | One API's plans, sorted by score out of 100, best first. Each plan comes with a rank and a price per 1,000 calls | `GET /apis/{slug}/best` |
| `datacircle_search_plans` | Plans that match a provider or plan name, on all APIs or on one API. You can sort them by score, provider or price | `GET /plans` |
| `datacircle_provider` | The APIs a provider sells, and the provider's rank on each API | `GET /providers/{slug}` |

## Install

The library is on [JitPack](https://jitpack.io/#waynehamadi/langchain4j-datacircle). With Maven:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.waynehamadi</groupId>
    <artifactId>langchain4j-datacircle</artifactId>
    <version>0.1.0</version>
</dependency>
```

With Gradle:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.waynehamadi:langchain4j-datacircle:0.1.0")
}
```

The library uses the LangChain4j your app already has, 1.0.0 or later, on Java 17 or later. It adds no other dependency.

## Use the tools in an AI service

```java
import dev.datacircle.langchain4j.DatacircleTools;
import dev.langchain4j.service.AiServices;

interface Assistant {
    String chat(String message);
}

Assistant assistant = AiServices.builder(Assistant.class)
        .chatModel(model)
        .tools(new DatacircleTools())
        .build();

String answer = assistant.chat("Who are the 3 best providers for the email-finder API?");
```

`tools(...)` takes your own tool objects too: `tools(new DatacircleTools(), new MyTools())`.

## Use the tools in an agent

With `langchain4j-agentic`, an agent takes them the same way:

```java
import dev.datacircle.langchain4j.DatacircleTools;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PlanFinder {
    @UserMessage("Answer with the tools you are given: {{request}}")
    @Agent("Finds the best plans of a GTM API")
    String find(@V("request") String request);
}

PlanFinder finder = AgenticServices.agentBuilder(PlanFinder.class)
        .chatModel(model)
        .tools(new DatacircleTools())
        .build();
```

## What the tools answer

Each tool answers in short text, one API or plan a line, with each plan's page on datacircle.dev. For example, `datacircle_best_plans` with `{"api": "email-finder", "limit": 3}` answers:

```text
Employee email (email-finder): the best 3 of 5,444 plans. Sheet: https://datacircle.dev/email-finder-api-pricing
1. Emelia, 100,000 credits per month: score 76.6/100, $4.99 per 1,000 calls, $0 per 1,000 not found, monthly. https://datacircle.dev/find-email-apis/emelia/19-monthly
2. ...
```

When a call fails, the tool answers with a line that starts with `Error:` and says what to do, for example when a slug does not exist. It does not throw.

## Options

`new DatacircleTools(baseUrl, timeout)` sets another base URL or another timeout. The defaults are `https://api.datacircle.dev/v1` and 30 seconds.

The API allows 60 calls a minute from one address. Its spec is at https://api.datacircle.dev/v1/openapi.json.

## Tests

```bash
mvn test                      # offline, on a local server
DATACIRCLE_LIVE=1 mvn test    # also calls each tool once on the live API
```

## License

MIT
