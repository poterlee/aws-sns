#####本ソースコードの使用方法
* Eclipseプロジェクト形式ですので、Eclipseへインポートすることができます。
* Maven Tomcat Pluginを使用しています。m2eclipseをご使用のEclipseへインストールし、Mavenのセットアップ(Installations, UserSettings)も同時に行ってください。
* Tomcat8(JDK1.8.0.45)で検証しています。

#####WARファイルの作成方法
* mvn clean install を実行するとtarget/配下にwarファイルが作成されます。
* gradle clean install を実行するとbuild/libs/配下にwarファイルが作成されます。

#####ローカル環境での動作検証方法
* target/配下にwarファイルが存在する状態（clean install 実行後) mvn tomcat7:run-warを実行してください。
* build/libsにwarファイルが存在する状態（clean install 実行後) gradle jettyRunWar を実行してください。

#####注意事項
* ソースコードの中には[AWSドキュメント](http://aws.amazon.com/jp/documentation/)に記載されているサンプルコードを使用しているものがあります。

#####JUnitテスト
* EclipseでJUnitテストを実行する場合はgradleタスク（Ctrl+Alt+Shift+R）で行ってください。
    * test　テストケース実行
    * jacocoTestReport カバレッジレポート作成(build/reports/jacoco) 

#####RELEASE
* 0.1.1-SNAPSHOT
    * Gradleプロジェクト形式に変換しました。
    * WARファイルの作成方法にgradle版を追記しました。
    * ローカル環境での動作検証サーバーをJettyに変更しました。
