function mapPromptToMessages(prompt) {
    var rules = getRules(prompt);

    var messages = [];

    // System prompts (loop)
    (prompt.getSystemPromptTexts() || []).forEach(function (systemText) {
        messages.push({role: 'system', content: systemText});
    });

    // Review prompts (loop)
    (prompt.getReviewPromptTexts() || []).forEach(function (reviewText) {
        messages.push({role: 'user', content: reviewText});
    });

    // Review target prompts (loop)
    (prompt.getReviewTargetPromptTexts() || []).forEach(function (targetText) {
        messages.push({role: 'user', content: targetText});
    });

    // File group prompts (loop)
    (prompt.getFileGroupPromptTexts() || []).forEach(function (fileGroupText) {
        messages.push({role: 'user', content: fileGroupText});
    });

    messages.push({role: 'user', content: 'The code review rules:'});
    messages.push({role: 'user', content: getRulesAsJson(rules)});

    var files = prompt.getFiles() || [];

    if (files.length === 1) {
        messages.push({role: 'user', content: 'Here is the file, presented as json and a code block:'});
    } else if (files.length > 1) {
        messages.push({role: 'user', content: 'Here are the ' + files.length + ' files, each presented as json and a code block:'});
    }

    files.forEach(function (file) {
        var fileInfo = getFileInfo(file);
        var fileInfoJson = getFileInfoJson(fileInfo);
        var fileInfoContent = getFileInfoContent(fileInfo)
        messages.push({role: 'user', content: fileInfoJson});
        messages.push({role: 'user', content: fileInfoContent});
    });

    return messages;
}

function getRules(prompt) {
    return prompt.getRules() || [];
}

function getRulesAsJson(rules) {
    return JSON.stringify(getMappedRules(rules));
}

function getMappedRules(rules) {
    var result = [];
    if (rules) {
        rules.forEach(function (rule) {
            result.push({
                id: rule.ruleKey.id,
                code: rule.ruleKey.code,
                description: rule.description
            });
        });
    }
    return result;
}

function getFileInfoContent(file) {
    return '```' + getCodeTypeByFileNameExtension(file.extension) + '\n' + file.content + '\n' + '```';
}

function getFileInfoJson(file) {
    return JSON.stringify(getFileInfoShort(file));
}

function getFileInfoShort(file) {
    return {
        id: file.id,
        name: file.name,
        type: getCodeTypeByFileNameExtension(file.extension),
        path: file.path,
        size: file.size,
        createdAt: file.createdAt,
        modifiedAt: file.modifiedAt
    };
}

function getFileInfo(file) {
    return {
        id: file && file.id ? file.id : null,
        name: file && file.metadata && file.metadata.fileName ? file.metadata.fileName : null,
        extension: file && file.metadata && file.metadata.fileNameExtension ? file.metadata.fileNameExtension : null,
        path: file && file.metadata && file.metadata.filePath ? file.metadata.filePath : null,
        size: file && file.metadata && file.metadata.fileSize ? file.metadata.fileSize : null,
        createdAt: file && file.metadata && file.metadata.createdAt ? file.metadata.createdAt : null,
        modifiedAt: file && file.metadata && file.metadata.modifiedAt ? file.metadata.modifiedAt : null,
        content: file && file.content ? file.content : ''
    };
}

function getCodeTypeByFileNameExtension(fileType) {
    const extensionToLanguage = {
        js: 'javascript',
        jsx: 'javascript',
        ts: 'typescript',
        tsx: 'typescript',
        py: 'python',
        java: 'java',
        html: 'html',
        css: 'css',
        json: 'json',
        xml: 'xml',
        c: 'c',
        cpp: 'cpp',
        cc: 'cpp',
        cxx: 'cpp',
        h: 'c', // Or 'cpp' for C++ headers
        hpp: 'cpp',
        cs: 'csharp',
        php: 'php',
        rb: 'ruby',
        go: 'go',
        swift: 'swift',
        sh: 'shell',
        bash: 'shell',
        zsh: 'shell',
        ksh: 'shell',
        md: 'markdown',
        markdown: 'markdown',
        yml: 'yaml',
        yaml: 'yaml',
        ini: 'ini',
        toml: 'toml',
        rs: 'rust',
        scala: 'scala',
        kt: 'kotlin',
        kts: 'kotlin',
        dart: 'dart',
        sql: 'sql',
        pl: 'perl',
        lua: 'lua',
        r: 'r',
        m: 'matlab', // Could also be Objective-C; context needed
        vb: 'vbnet',
        asm: 'assembly',
        s: 'assembly',
        scss: 'scss',
        less: 'less',
        styl: 'stylus',
        vue: 'vue',
        coffee: 'coffeescript',
        dockerfile: 'docker',
        makefile: 'makefile',
        cmake: 'cmake',
        bat: 'batch',
        ps1: 'powershell',
        groovy: 'groovy',
        gradle: 'gradle',
        tex: 'latex',
        latex: 'latex',
        svg: 'svg',
        txt: 'plaintext',
        log: 'plaintext'
    };

    if (fileType && typeof fileType.toLowerCase === 'function') {
        var key = fileType.toLowerCase();
        if (extensionToLanguage[key]) {
            return extensionToLanguage[key];
        }
    }
    return 'plaintext';
}
