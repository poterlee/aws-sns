#####本ソースコードの使用方法
* Eclipseプロジェクト形式ですので、Eclipseへインポートすることができます。
* Maven Tomcat Pluginを使用しています。m2eclipseをご使用のEclipseへインストールし、Mavenのセットアップ(Installations, UserSettings)も同時に行ってください。
* Tomcat8(JDK1.8.0.45)で検証しています。

#####WARファイルの作成方法
* mvn clean install を実行するとtarget/配下にwarファイルが作成されます。

#####ローカル環境での動作検証方法
* target/配下にwarファイルが存在する状態（clean install 実行後) mvn tomcat7:run-warを実行してください。

#####注意事項
* ソースコードの中には[AWSドキュメント](http://aws.amazon.com/jp/documentation/)に記載されているサンプルコードを使用しているものがあります。
