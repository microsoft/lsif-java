function isWin() {
	return /^win/.test(process.platform);
}

function mvnw() {
	return isWin() ? 'mvnw.cmd' : './mvnw';
}

const cp = require('child_process');
const path = require('path');
const fse = require('fs-extra');
const glob = require('glob');
const rimraf = require('rimraf');
const rootPath = path.join(__dirname, '..', '..');

rimraf.sync(path.join(__dirname, 'repository'));
rimraf.sync(path.join(__dirname, 'workspace'));
const tgzs = glob.sync('*.tgz');
for (const tgz of tgzs) {
	fse.removeSync(tgz);
}

cp.execSync(`${mvnw()} clean verify`, { cwd: rootPath, stdio: [0, 1, 2] });
const sourceRepositoryPath = path.join(rootPath, 'com.microsoft.java.lsif.product', 'target', 'repository');
const targetRepositoryPath = path.join(rootPath, 'cmd', 'repository');

fse.copySync(path.join(sourceRepositoryPath, 'config_linux'), path.join(targetRepositoryPath, 'config_linux'));
fse.copySync(path.join(sourceRepositoryPath, 'config_mac'), path.join(targetRepositoryPath, 'config_mac'));
fse.copySync(path.join(sourceRepositoryPath, 'config_win'), path.join(targetRepositoryPath, 'config_win'));
fse.copySync(path.join(sourceRepositoryPath, 'features'), path.join(targetRepositoryPath, 'features'));
fse.copySync(path.join(sourceRepositoryPath, 'plugins'), path.join(targetRepositoryPath, 'plugins'));
