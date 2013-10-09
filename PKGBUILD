# Maintainer: Yanus Poluektovich <ypoiluektovich@gmail.com>
pkgname=reinforce
pkgver=0.1.7
pkgrel=1
pkgdesc="A build tool for java"
arch=('any')
url="https://github.com/ypoluektovich/reinforce"
license=('unknown')
depends=('java-environment>=7' 'junit>=4.11-3' 'apache-ivy>=2.3.0' 'java-testng>=6.8')
source=(
    "https://github.com/ypoluektovich/reinforce/archive/v$pkgver.tar.gz"
    "ivysettings-local.xml"
    "uselocalivy.patch"
    "rein"
    )
noextract=()
sha512sums=('76236bf46c582ceb22104c525958a18983263bdf5f8e6b14a9c17baad1f2583ad13c08e0f988399f1a4c0f38682a97c61f788d624e8b732ead2d141b47919813'
            'e1410bae91d04b0a0f194655a542e42f34ee8cebf6b0bb8606a9ed54523183aef6d436471d7445de34c93082d057f15409f87c18a8b2ea4cd511a10590779748'
            '283a4cc25cdaaf0047132ff426765f539d356f8f03068972b8375e231169c5f3cd7015ca2934e4bf1b1fde1359b84173289eeda54bd6179804dd5412116ab8d0'
            '96c231db93b70e41ee72676640a5f137b55bfe62148153aec049733d6400b068dee2365f8ef73ccb6cef1bc821a255fd05708408da7c77577e27066021cf7771')

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
  install -D        rein                               "${pkgdir}/usr/bin/rein"
  cd "${pkgname}-${pkgver}"
  install -D -m644  build/reinforce.jar                "${pkgdir}/usr/share/java/reinforce/reinforce.jar"
  install -D -m644  lib/snakeyaml-1.11.jar             "${pkgdir}/usr/share/java/reinforce/snakeyaml.jar"

  install -D -m644  "src/command-line-completion/bash"  "${pkgdir}/usr/share/bash-completion/completions/rein"
}
