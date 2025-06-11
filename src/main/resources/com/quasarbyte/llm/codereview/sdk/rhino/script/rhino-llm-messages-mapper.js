function mapPromptToMessages(prompt) {
    var rules = getRules(prompt);

    var messages = [];

    // System prompts (loop)
    var systemPrompts = prompt.getSystemPromptTexts() || [];
    for (var i = 0; i < systemPrompts.length; i++) {
        messages.push({ role: 'system', content: systemPrompts[i] });
    }

    // Review prompts (loop)
    var reviewPrompts = prompt.getReviewPromptTexts() || [];
    for (var i = 0; i < reviewPrompts.length; i++) {
        messages.push({ role: 'user', content: reviewPrompts[i] });
    }

    // Review target prompts (loop)
    var targetPrompts = prompt.getReviewTargetPromptTexts() || [];
    for (var i = 0; i < targetPrompts.length; i++) {
        messages.push({ role: 'user', content: targetPrompts[i] });
    }

    // File group prompts (loop)
    var fileGroupPrompts = prompt.getFileGroupPromptTexts() || [];
    for (var i = 0; i < fileGroupPrompts.length; i++) {
        messages.push({ role: 'user', content: fileGroupPrompts[i] });
    }

    messages.push({ role: 'user', content: 'The code review rules:' });
    messages.push({ role: 'user', content: getRulesAsJson(rules) });

    var files = prompt.getFiles() || [];

    if (files.length === 1) {
        messages.push({ role: 'user', content: 'Here is the file, presented as json and a code block:' });
    } else if (files.length > 1) {
        messages.push({ role: 'user', content: 'Here are the ' + files.length + ' files, each presented as json and a code block:' });
    }

    for (var i = 0; i < files.length; i++) {
        var fileInfo = getFileInfo(files[i]);
        var fileInfoJson = getFileInfoJson(fileInfo);
        var fileInfoContent = getFileInfoContent(fileInfo);

        messages.push({
            role: 'user',
            content: '=== FILE METADATA ===\n' + fileInfoJson + '\n\n=== FILE CONTENT ===\n' + fileInfoContent
        });
    }

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
        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];
            result.push({
                id: rule.ruleKey.id,
                code: rule.ruleKey.code,
                description: rule.description
            });
        }
    }
    return result;
}

function getFileInfoContent(file) {
    var codeType = getCodeTypeByFile(file);
    var content = file.content ? file.content : '';
    return '```' + codeType + '\n' + content + '\n```';
}

function getFileInfoJson(file) {
    return JSON.stringify(getFileInfoShort(file));
}

function getFileInfoShort(file) {
    return {
        id: file.id,
        name: file.name,
        type: getCodeTypeByFile(file),
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

function getCodeTypeByFile(file) {
    if (file && file.extension && file.extension.trim() !== '') {
        return getCodeTypeByFileNameExtension(file.extension);
    } else {
        return file && file.name && file.name.trim() !== '' ? file.name : 'plaintext';
    }
}

function getCodeTypeByFileNameExtension(fileType) {
    if (fileType) {
        var extensionToLanguage = {
            asm: 'assembly',
            astro: 'astro',
            bat: 'batch',
            bash: 'shell',
            c: 'c',
            cc: 'cpp',
            cmake: 'cmake',
            coffee: 'coffeescript',
            cpp: 'cpp',
            cs: 'csharp',
            css: 'css',
            cxx: 'cpp',
            dart: 'dart',
            dockerfile: 'docker',
            go: 'go',
            gradle: 'gradle',
            groovy: 'groovy',
            h: 'c',
            hcl: 'hcl',
            html: 'html',
            hpp: 'cpp',
            ini: 'ini',
            java: 'java',
            js: 'javascript',
            json: 'json',
            jsx: 'javascript',
            kt: 'kotlin',
            ksh: 'shell',
            kts: 'kotlin',
            latex: 'latex',
            less: 'less',
            log: 'plaintext',
            lua: 'lua',
            m: 'matlab',
            makefile: 'makefile',
            markdown: 'markdown',
            md: 'markdown',
            php: 'php',
            pl: 'perl',
            properties: 'properties',
            ps1: 'powershell',
            py: 'python',
            r: 'r',
            rb: 'ruby',
            rs: 'rust',
            sass: 'sass',
            scala: 'scala',
            scss: 'scss',
            sh: 'shell',
            sql: 'sql',
            styl: 'stylus',
            svelte: 'svelte',
            svg: 'svg',
            swift: 'swift',
            tex: 'latex',
            toml: 'toml',
            ts: 'typescript',
            tsx: 'typescript',
            txt: 'plaintext',
            vb: 'vbnet',
            vue: 'vue',
            xml: 'xml',
            yaml: 'yaml',
            yml: 'yaml',
            zig: 'zig',
            zsh: 'shell'
        };

        var key = fileType.toLowerCase();
        if (extensionToLanguage[key]) {
            return extensionToLanguage[key];
        }
    }

    return 'plaintext';
}
