# Maintainer: Yanus Poluektovich <ypoiluektovich@gmail.com>
pkgname=reinforce
pkgver=0.1.4
pkgrel=1
pkgdesc="A build tool for java"
arch=('any')
url="https://github.com/ypoluektovich/reinforce"
license=('unknown')
depends=('java-environment>=7' 'junit>=4.11-3' 'apache-ivy>=2.3.0')
source=(
    "https://github.com/ypoluektovich/reinforce/archive/v$pkgver.tar.gz"
    "ivysettings-local.xml"
    "uselocalivy.patch"
    "rein"
    )
noextract=()
sha512sums=('b1c415e5bf8ef0909bfb26bb06c45105b67f6dc2db56c7feca0ed283ad6beb61dc155adb959ada777781f0814393bab872deed0cf486965d15f306cd5f964102'
            'e1410bae91d04b0a0f194655a542e42f34ee8cebf6b0bb8606a9ed54523183aef6d436471d7445de34c93082d057f15409f87c18a8b2ea4cd511a10590779748'
            '283a4cc25cdaaf0047132ff426765f539d356f8f03068972b8375e231169c5f3cd7015ca2934e4bf1b1fde1359b84173289eeda54bd6179804dd5412116ab8d0'
            '00ced3b684efeeeba491e7aca41ade3a4ee050ae1d6a744404388e713796a277e5a5a19dfccab43a62e526bf21c2c43a49af7f04ada7fbb94d5447a0ab661825')

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
