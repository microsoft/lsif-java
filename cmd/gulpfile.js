const gulp = require('gulp');
const cp = require('child_process');
const path = require('path');
const fs = require('fs');
const glob = require('glob');
const rimraf = require("rimraf");

const rootPath = path.join(__dirname, '..');

gulp.task('clean', (done) => {
    rimraf.sync(path.join(__dirname, 'repository'));
    rimraf.sync(path.join(__dirname, 'workspace'));
    const packages = glob.sync('*.tgz');
    for (const package of packages) {
        fs.unlinkSync(package);
    }
    done();
});

gulp.task('build-indexer', (done) => {
    cp.execSync(`${mvnw()} clean verify`, { cwd: rootPath, stdio: [0, 1, 2] });
    gulp.src(path.join(rootPath, 'com.microsoft.java.lsif.product', 'target', 'repository', '**/*'))
        .pipe(gulp.dest(path.join(rootPath, 'cmd', 'repository')));
    done();
});

gulp.task('build', gulp.series('clean', 'build-indexer'));

function isWin() {
    return /^win/.test(process.platform);
}

function mvnw() {
    return isWin() ? 'mvnw.cmd' : './mvnw';
}