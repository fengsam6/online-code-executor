document.addEventListener('DOMContentLoaded', function() {
    // 初始化代码编辑器
    const editor = CodeMirror.fromTextArea(document.getElementById('code'), {
        lineNumbers: true,
        theme: 'monokai',
        mode: 'text/x-java',
        indentUnit: 4,
        autoCloseBrackets: true,
        matchBrackets: true,
        lineWrapping: true
    });

    // 语言切换处理
    const languageSelect = document.getElementById('language');
    const modeMap = {
        'java': 'text/x-java',
        'cpp': 'text/x-c++src',
        'c': 'text/x-csrc',
        'python': 'text/x-python',
        'go': 'text/x-go'
    };

    // 示例代码
    const examples = {
        java: {
            basic: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
            input: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入你的名字：");
        String name = scanner.nextLine();
        System.out.println("你好, " + name + "!");
    }
}`,
            array: `import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] numbers = {5, 2, 8, 1, 9};
        Arrays.sort(numbers);
        System.out.println("排序后：" + Arrays.toString(numbers));
    }
}`
        },
        cpp: {
            basic: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, World!" << endl;
    return 0;
}`,
            input: `#include <iostream>
#include <string>
using namespace std;

int main() {
    string name;
    cout << "请输入你的名字：";
    getline(cin, name);
    cout << "你好, " << name << "!" << endl;
    return 0;
}`,
            array: `#include <iostream>
#include <algorithm>
#include <vector>
using namespace std;

int main() {
    vector<int> numbers = {5, 2, 8, 1, 9};
    sort(numbers.begin(), numbers.end());
    cout << "排序后：";
    for(int num : numbers) {
        cout << num << " ";
    }
    cout << endl;
    return 0;
}`
        },
        c: {
            basic: `#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    return 0;
}`,
            input: `#include <stdio.h>

int main() {
    char name[50];
    printf("请输入你的名字：");
    scanf("%s", name);
    printf("你好, %s!\\n", name);
    return 0;
}`,
            array: `#include <stdio.h>
#include <stdlib.h>

int compare(const void* a, const void* b) {
    return (*(int*)a - *(int*)b);
}

int main() {
    int numbers[] = {5, 2, 8, 1, 9};
    int size = sizeof(numbers) / sizeof(numbers[0]);
    qsort(numbers, size, sizeof(int), compare);
    printf("排序后：");
    for(int i = 0; i < size; i++) {
        printf("%d ", numbers[i]);
    }
    printf("\\n");
    return 0;
}`
        },
        python: {
            basic: `print("Hello, World!")`,
            input: `name = input("请输入你的名字：")
print(f"你好, {name}!")`,
            array: `numbers = [5, 2, 8, 1, 9]
numbers.sort()
print(f"排序后：{numbers}")`
        },
        go: {
            basic: `package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}`,
            input: `package main

import (
    "fmt"
    "bufio"
    "os"
    "strings"
)

func main() {
    reader := bufio.NewReader(os.Stdin)
    fmt.Print("请输入你的名字：")
    name, _ := reader.ReadString('\\n')
    name = strings.TrimSpace(name)
    fmt.Printf("你好, %s!\\n", name)
}`,
            array: `package main

import (
    "fmt"
    "sort"
)

func main() {
    numbers := []int{5, 2, 8, 1, 9}
    sort.Ints(numbers)
    fmt.Printf("排序后：%v\\n", numbers)
}`
        }
    };

    // 语言切换事件
    languageSelect.addEventListener('change', function() {
        const lang = this.value;
        editor.setOption('mode', modeMap[lang]);
        editor.setValue(examples[lang].basic);
    });

    // 运行代码
    const runBtn = document.getElementById('runBtn');
    const loading = document.getElementById('loading');
    const output = document.getElementById('output');
    const input = document.getElementById('input');

    runBtn.addEventListener('click', async function() {
        loading.classList.remove('d-none');
        runBtn.disabled = true;
        output.textContent = '运行中...';

        try {
            const response = await fetch('/api/execute', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    language: languageSelect.value,
                    code: editor.getValue(),
                    input: input.value
                })
            });

            const result = await response.json();
            if (result.success) {
                output.textContent = result.output || '程序执行成功，无输出';
                output.classList.remove('text-danger');
            } else {
                output.textContent = result.error || '执行失败';
                output.classList.add('text-danger');
            }
        } catch (error) {
            output.textContent = '请求失败: ' + error.message;
            output.classList.add('text-danger');
        } finally {
            loading.classList.add('d-none');
            runBtn.disabled = false;
        }
    });

    // 初始化默认代码
    editor.setValue(examples.java.basic);

    // 保存代码
    const saveBtn = document.getElementById('saveBtn');
    saveBtn.addEventListener('click', async function() {
        const title = prompt('请输入代码标题：');
        if (!title) return;

        const snippet = {
            language: languageSelect.value,
            code: editor.getValue(),
            input: input.value,
            title: title,
            createTime: new Date().toISOString()
        };

        try {
            const response = await fetch('/api/snippets', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(snippet)
            });

            if (response.ok) {
                alert('保存成功！');
            } else {
                throw new Error('保存失败');
            }
        } catch (error) {
            alert('保存失败: ' + error.message);
        }
    });

    // 加载代码
    const loadBtn = document.getElementById('loadBtn');
    loadBtn.addEventListener('click', async function() {
        try {
            const response = await fetch('/api/snippets');
            const snippets = await response.json();

            if (snippets.length === 0) {
                alert('没有保存的代码');
                return;
            }

            const snippet = await showSnippetSelector(snippets);
            if (snippet) {
                languageSelect.value = snippet.language;
                editor.setValue(snippet.code);
                input.value = snippet.input || '';
                editor.setOption('mode', modeMap[snippet.language]);
            }
        } catch (error) {
            alert('加载失败: ' + error.message);
        }
    });

    // 显示代码选择器
    function showSnippetSelector(snippets) {
        return new Promise((resolve) => {
            const modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">选择要加载的代码</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="list-group">
                                ${snippets.map(s => `
                                    <button class="list-group-item list-group-item-action" data-id="${s.id}">
                                        ${s.title} (${s.language})
                                        <br>
                                        <small class="text-muted">
                                            ${new Date(s.createTime).toLocaleString()}
                                        </small>
                                    </button>
                                `).join('')}
                            </div>
                        </div>
                    </div>
                </div>
            `;

            document.body.appendChild(modal);
            const modalInstance = new bootstrap.Modal(modal);
            modalInstance.show();

            modal.addEventListener('click', async (e) => {
                const button = e.target.closest('.list-group-item');
                if (button) {
                    const id = button.dataset.id;
                    const response = await fetch(`/api/snippets/${id}`);
                    const snippet = await response.json();
                    modalInstance.hide();
                    modal.remove();
                    resolve(snippet);
                }
            });

            modal.addEventListener('hidden.bs.modal', () => {
                modal.remove();
                resolve(null);
            });
        });
    }

    // 添加代码提示按钮事件
    const hintBtn = document.getElementById('hintBtn');
    hintBtn.addEventListener('click', function() {
        const lang = languageSelect.value;
        showCodeExamples(lang, examples[lang]);
    });

    // 显示代码示例对话框
    function showCodeExamples(language, examples) {
        const modal = document.createElement('div');
        modal.className = 'modal fade';
        modal.innerHTML = `
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">${language.toUpperCase()} 代码示例</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="list-group">
                            ${Object.entries(examples).map(([key, code]) => `
                                <button class="list-group-item list-group-item-action">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h6 class="mb-0">${getExampleTitle(key)}</h6>
                                        <button class="btn btn-sm btn-primary use-btn">使用此示例</button>
                                    </div>
                                    <pre class="mt-2"><code>${code}</code></pre>
                                </button>
                            `).join('')}
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(modal);
        const modalInstance = new bootstrap.Modal(modal);
        modalInstance.show();

        // 点击使用示例按钮
        modal.querySelectorAll('.use-btn').forEach((btn, index) => {
            btn.addEventListener('click', () => {
                editor.setValue(Object.values(examples)[index]);
                modalInstance.hide();
            });
        });

        modal.addEventListener('hidden.bs.modal', () => {
            modal.remove();
        });
    }

    // 获取示例标题
    function getExampleTitle(key) {
        const titles = {
            basic: '基础示例',
            input: '输入示例',
            array: '数组排序'
        };
        return titles[key] || key;
    }
}); 