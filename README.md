# Gboard iOS-style JP/EN Patches (for Morphe)

Gboard の日本語フリックと英語キーボードを iPhone 風の配置にする、非公式の [Morphe](https://morphe.software) 用パッチです。

![スクリーンショット: 上段は日本語のかな・ABC・123、下段は英語の英字・123・#+=](.github/assets/screenshots.png)

## 使い方

1. Morphe Manager にこのリポジトリをソースとして追加します。

   <a href="https://morphe.software/add-source?github=twelvehouse/gboard-ios-style-jp-en-morphe-patches">
     <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
   </a>

   または `https://github.com/twelvehouse/gboard-ios-style-jp-en-morphe-patches` を手で追加します。
2. 対応する版の Gboard（下のパッチ一覧を参照）にパッチを当てて、インストールします。
3. Android の設定で、パッチを当てた Gboard を有効にして選びます。

## おすすめの設定

Gboard の設定で次のようにすると、スクリーンショットと同じになります。

- **言語 → 日本語 → 12 キー → フリックのみ**: オン（同じ文字を続けて入力できます）
- **Preferences → Number row**: オン（英字キーボードの上に数字の段）
- **Preferences → Comma key / Period key**: オフ

## 注意

- パッケージ名を変えるので、元の Gboard とは別のアプリとして入ります。
- 新しい版を上書きインストールしても配置が変わらないときは、パッチを当てた Gboard のデータを消してください（Gboard がキーボードの定義をデータに保存しているためです）。
- 上書きインストールするには、前回と同じ鍵で署名する必要があります。

## パッチ一覧

<!-- PATCHES_START EXPANDED -->

<!-- このあいだは、リリースのときに patches-list.json から自動で作られます。手で書き換えないでください。 -->

<!-- PATCHES_END -->
