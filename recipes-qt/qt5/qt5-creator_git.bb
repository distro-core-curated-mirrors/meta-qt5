SUMMARY = "Qt Creator is a new cross-platform Qt IDE"

# Note:
# The toolchain auto detection does not work completely yet. To compile/debug
# open menu 'Tools/Options and select 'Build & Run'. In tab 'Kits' select 'Desktop'
# 'Compiler/Manage...' and add local gcc'. At 'Debugger' select
# 'System GDB at /usr/bin/gdb.

HOMEPAGE = "https://qt-project.org/"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = " \
    file://LICENSE.GPL3-EXCEPT;md5=763d8c535a234d9a3fb682c7ecb6c073 \
"

inherit qmake5

DEPENDS = "qtbase qtscript qtwebkit qtxmlpatterns qtx11extras qtdeclarative qttools qttools-native qtsvg chrpath-replacement-native"
DEPENDS_append_libc-musl = " libexecinfo"

SRCREV = "8768e39d3c8e74e583eca3897cc6de53a99c3dde"
PV = "4.7.1+git${SRCPV}"

# Patches from https://github.com/meta-qt5/qtcreator/commits/b4.7.1
# 4.7.1.meta-qt5.1
SRC_URI = " \
    git://code.qt.io/qt-creator/qt-creator.git;branch=4.7 \
    file://0001-Use-correct-path-prefix.patch \
    file://0002-botan.pro-pass-QMAKE_AR.patch \
    file://qtcreator.desktop.in \
"
SRC_URI_append_libc-musl = " file://0003-Link-with-libexecinfo-on-musl.patch"

S = "${WORKDIR}/git"

EXTRA_QMAKEVARS_PRE += "IDE_LIBRARY_BASENAME=${baselib}${QT_DIR_NAME}"

EXTRANATIVEPATH += "chrpath-native"

do_configure_append() {
    # Find native tools
    sed -i 's:${STAGING_BINDIR}.*/qdoc:${OE_QMAKE_PATH_EXTERNAL_HOST_BINS}/qdoc:g' ${B}/Makefile
}

do_install() {
    oe_runmake install INSTALL_ROOT=${D}${prefix}
    oe_runmake install_inst_qch_docs INSTALL_ROOT=${D}${prefix}
    # install desktop and ensure that qt-creator finds qmake
    install -d ${D}${datadir}/applications
    install -m 0644 ${WORKDIR}/qtcreator.desktop.in ${D}${datadir}/applications/qtcreator.desktop
    sed -i 's:@QT5_QMAKE@:${OE_QMAKE_PATH_QT_BINS}:g' ${D}${datadir}/applications/qtcreator.desktop
    chrpath --delete ${D}${libexecdir}/qtcreator/qtcreator_process_stub
    chrpath --delete ${D}${libexecdir}/qtcreator/qbs_processlauncher
    chrpath --delete ${D}${libdir}/${QT_DIR_NAME}/qtcreator/libqbscore.so.*
    test -e ${D}${libdir}/${QT_DIR_NAME}/qtcreator/plugins/qmldesigner/libcomponentsplugin.so && chrpath --delete ${D}${libdir}/${QT_DIR_NAME}/qtcreator/plugins/qmldesigner/libcomponentsplugin.so
    test -e ${D}${libdir}/${QT_DIR_NAME}/qtcreator/plugins/qmldesigner/libqtquickplugin.so && chrpath --delete ${D}${libdir}/${QT_DIR_NAME}/qtcreator/plugins/qmldesigner/libqtquickplugin.so
}

FILES_${PN} += " \
    ${datadir}/qtcreator \
    ${datadir}/metainfo \
    ${datadir}/icons \
    ${libdir}${QT_DIR_NAME}/qtcreator \
"

FILES_${PN}-dev += " \
    ${libdir}${QT_DIR_NAME}/qtcreator/*${SOLIBSDEV} \
"

RDEPENDS_${PN} += "perl python"
RCONFLICTS_${PN} = "qt-creator"

# To give best user experience out of the box..
RRECOMMENDS_${PN} += " \
    packagegroup-qt5-toolchain-target \
    binutils \
    ccache \
    make \
    gcc-symlinks g++-symlinks cpp-symlinks \
    gdb \
"

# ERROR: qt5-creator-4.5.1-r0 do_package_qa: QA Issue: No GNU_HASH in the elf binary: '/OE/build/oe-core/tmp-glibc/work/core2-64-oe-linux/qt5-creator/4.5.1-r0/packages-split/qt5-creator/usr/lib/qt5/qtcreator/libqbscore.so.1.10.1'
INSANE_SKIP_${PN} += "ldflags"