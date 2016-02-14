'use strict';

var fs = require('fs');
var marked = require('marked');

const TEMPLATE_REGEX = /{{.+}}/ig;
const BRACKET_REGEX = /(\{|\})/ig;

var inputFilePath = process.argv[2];
if (!inputFilePath) {
    console.error('You must pass in a file as first parameter');
    process.exit(1);
}

marked.setOptions({
    renderer: new marked.Renderer(),
    gfm: true,
    tables: true,
    breaks: true
});

var templateContent = fs.readFileSync('_template.html', { encoding: 'utf8' });
var markdown = fs.readFileSync(inputFilePath, { encoding: 'utf8' });
var contentMap = {};

var html = marked(markdown);

contentMap[inputFilePath] = html;

var processed = templateContent.replace(TEMPLATE_REGEX, function(template) {
    var key = template.substr(2, template.length - 4).trim();
    return contentMap[key];
});

fs.writeFileSync('processed.html', processed);

console.log('Template replaced with markdown. Exiting.');

function isString(value) {
    return (typeof value === 'string' || value instanceof String);
}
