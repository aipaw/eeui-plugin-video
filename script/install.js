// 安装插件时会node运行此文件
const fs = require('fs');
const path = require('path');

let workPath = process.cwd();

function __ios() {
    let appDelegateFile = path.join(workPath, 'platforms/ios/eeuiApp/eeuiApp/AppDelegate.m');
    if (!fs.existsSync(appDelegateFile)) {
        return;
    }
    let appDelegateResult = fs.readFileSync(appDelegateFile, 'utf8');
    if (!appDelegateResult.match(/\)\s*application\s*supportedInterfaceOrientationsForWindow\s*:/)) {
        let lastIndex = appDelegateResult.lastIndexOf("@end");
        if (lastIndex != -1) {
            let repositories = "//配置屏幕可旋转方向\n- (UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {\n    return UIInterfaceOrientationMaskAll;\n}\n\n";
            let newResult = appDelegateResult.slice(0, lastIndex) + repositories + appDelegateResult.slice(lastIndex);
            fs.writeFileSync(appDelegateFile, newResult, 'utf8');
        }
    }
}

__ios();
