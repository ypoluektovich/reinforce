# Maintainer: Yanus Poluektovich <ypoiluektovich@gmail.com>
pkgname=reinforce
pkgver=0.1.0
pkgrel=1
pkgdesc="A build tool for java"
arch=('any')
url="https://github.com/ypoluektovich/reinforce"
license=('unknown')
depends=('java-environment>=7' 'junit>=4.11-3' 'apache-ivy>=2.3.0')
conflicts=('reinforce')
source=(
    "https://github.com/ypoluektovich/reinforce/archive/v$pkgver.tar.gz"
    "ivysettings-local.xml"
    "uselocalivy.patch"
    "rein"
    )
noextract=()
md5sums=('24d9d03932ffbaeae6583ae459330eae'
         '1f3449a3b7ebcdd811e5e8056bfb3519'
         '0c8db58f3be81f5dee5ae592cb3e296f'
         '364109ee807c4bef7db1a72038171d8c')

build() {
  cd "${srcdir}/${pkgname}-${pkgver}"
  msg2 "Patching..."
  patch -p1 <../uselocalivy.patch
  msg2 "Building..."
  ./build-full.sh
}

check() {
  cd "${srcdir}/${pkgname}-${pkgver}"

  local _rj="build/reinforce.jar"
  msg2 "Checking file: %s" "${_rj}"
  if [[ ! -e "${_rj}" ]]; then
    error "The build process has changed: I can't find the jar file!"
    return 1
  fi
  if [[ ! -e "${_rj}" ]]; then
    error "Why is ${_rj} not a file?"
    return 1
  fi
  if [[ ! -s "${_rj}" ]]; then
    error "Why is ${_rj} empty?"
    return 1
  fi

  msg2 "Passed all checks"
}

package() {
  cd "${srcdir}"
  install -D        rein                    "${pkgdir}/usr/bin/rein"
  cd "${pkgname}-${pkgver}"
  install -D -m644  build/reinforce.jar     "${pkgdir}/usr/share/java/reinforce/reinforce.jar"
  install -D -m644  lib/snakeyaml-1.11.jar  "${pkgdir}/usr/share/java/reinforce/snakeyaml.jar"
}
