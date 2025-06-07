**llm-codereview-sdk** A modular Java SDK for automated code review powered by large language models (LLM).

This SDK provides a unified API for analyzing source code using different LLM providers, both cloud-based (e.g., OpenAI, Azure OpenAI) and local (e.g., Ollama, LM Studio).
It offers a set of core services, data models, and extensible interfaces to help engineers build custom solutions for code quality analysis and review automation, tailored to their infrastructure and requirements.

#### 1. Value Proposition and Purpose

**llm-codereview-sdk** is designed for developers, engineering teams, and companies looking to automate source code analysis and review processes using modern large language models (LLMs).

Key benefits of the SDK:

* Unified LLM integration with a single interface.
* Easy integration into your own tools, desktop/CLI applications, or internal services.
* Ready-to-use models and service interfaces for code, rules, batching, and review results.
* Scalable and extensible architecture, suitable for large codebases and custom workflows.
* A robust foundation for building your own code review automation platform or integrating with your code quality pipelines and systems.

Note:
This SDK is a library, not a standalone product or CLI tool. It does not include out-of-the-box CI/CD integrations or repository connectors, but is intended as a core building block for your own solutions and automations.

#### 2. Architecture and Core Concepts
**llm-codereview-sdk** is structured as a modular Java library exposing abstractions and services for LLM-based code review automation.

Core concepts include:

* A service layer with a central interface for launching code reviews.
* Domain models representing source files, analysis rules, review parameters, comments, and aggregated results.
* Extensible interfaces for supporting various LLM providers and review workflows.
* Built-in batching and parallel processing for efficient handling of large codebases.
* Clear separation of concerns for easy extension and customization.
* The SDK processes your code, applies rules, interacts with the LLM, and returns structured results containing comments, findings, and usage data.

#### 3. Working with Different LLM Providers

**llm-codereview-sdk** is provider-agnostic. It supports cloud-based LLMs (OpenAI, Azure OpenAI, etc.) and local/self-hosted solutions (Ollama, LM Studio, and more).

* Flexible client model: configure the SDK for any LLM provider using built-in interfaces.
* Plug-and-play support: switch providers or add your own by implementing/configuring a client.
* No vendor lock-in: freely use commercial or local LLMs with the correct configuration.
* The SDK does not enforce any specific provider or network transport. Its abstraction layer enables you to adapt integration as needed.

#### 4. Quick Start

Add the SDK as a dependency to your project using your build system of choice.
Configure your LLM client, prepare review parameters (such as files, rules, and options), and invoke the review service to receive structured results.
Please see the documentation and examples directory for detailed configuration and usage scenarios.

#### 5. Core Features

* Unified integration with any supported LLM provider.
* Flexible file handling, including glob patterns and symlink support.
* Comprehensive rule management and custom analysis.
* Batching and parallel execution for performance and scalability.
* Structured aggregation of review results and findings.
* Extensible architecture for custom rules, prompt mappers, and LLM clients.
* Configurable review workflow and resource management.
* The SDK is intentionally designed as a flexible foundation for your own review automation flows.

#### 6. Domain Model Architecture

The domain model defines the structure and flow of data for code review:

* ReviewParameter: configures the code review session.
* LlmClient: manages LLM provider connection details.
* Rule: describes analysis logic and parameters.
* ReviewResult: aggregates output—reviewed files, comments, rule violations, and usage stats.
* ReviewComment: captures individual comments or suggestions.
* Additional models (FileGroup, ReviewTarget, AggregatedResult, etc.) support batching, parallelism, and advanced workflows.

See the Javadoc or model source files for a detailed description of each model.

#### 7. Usage Examples

You can find real usage scenarios in the test sources and example files included in this repository.

These examples demonstrate how to:

* Review code with different LLM providers (OpenAI, Azure, Ollama, LM Studio, and others)
* Define and use custom analysis rules
* Apply batching and parallel processing

For more, see the src/test/java/ and src/test/resources/ directories.

#### 8. Testing

To understand how to work with the SDK, check out the existing tests in the src/test/java/ directory.
They show practical ways to configure, launch, and process results from the SDK in various scenarios.

Note:
For maximum compatibility with legacy codebases, the minimum required Java version is JDK 1.8.

#### 9. Development and Contributing

Contributions are welcome! The project is organized as follows:

* src/main/java/ — core SDK code (services, models, interfaces, utilities)
* src/test/java/ — unit and integration tests
* src/main/resources/ — scripts, configs, templates
* docs/ — documentation and style guides

To get started, clone the repository, set up your environment (JDK 1.8+ and Maven), and build the project.

Feel free to open issues or pull requests. Please follow the code style and include appropriate tests for any changes.

#### 10. License

This project is licensed under the Apache License, Version 2.0.
For details, see the LICENSE file or:
https://www.apache.org/licenses/LICENSE-2.0

#### 11. Support and Contact

If you have questions about using or extending llm-codereview-sdk, need help with integration, want to discuss custom software development, collaboration, or have any other inquiries, please contact:

* Email: taluyev+llm-code-review@gmail.com
* LinkedIn: linkedin.com/in/taluyev

#### 12. Frequently Asked Questions (FAQ)

* Is this a CLI tool? No, this is a Java library for developers building their own code review solutions.
* Which LLM providers are supported? Any provider can be used via configuration and client implementation, including OpenAI, Azure, Ollama, and LM Studio.
* Does the SDK work with private/local LLM deployments? Yes, you can connect to local LLM servers by supplying their API endpoints.
