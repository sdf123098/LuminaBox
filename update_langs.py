import json
import os
import glob

lang_dir = 'D:/Project/MCMOD/custom-bgm/common/src/main/resources/assets/luminabox/lang'
files = glob.glob(os.path.join(lang_dir, '*.json'))

translations = {
    'en_us': {
        'gui.luminabox.settings.proxy_title': 'HTTP Proxy (Requires Restart):',
        'gui.luminabox.settings.proxy_host': 'Host',
        'gui.luminabox.settings.proxy_port': 'Port',
        'gui.luminabox.settings.save_proxy': 'Save',
        'gui.luminabox.settings.proxy_saved': 'Proxy saved! Restart game to apply.'
    },
    'zh_cn': {
        'gui.luminabox.settings.proxy_title': 'HTTP 代理 (需重启生效):',
        'gui.luminabox.settings.proxy_host': '主机地址',
        'gui.luminabox.settings.proxy_port': '端口',
        'gui.luminabox.settings.save_proxy': '保存',
        'gui.luminabox.settings.proxy_saved': '代理设置已保存！重启游戏生效。'
    },
    'zh_tw': {
        'gui.luminabox.settings.proxy_title': 'HTTP 代理 (需重啟生效):',
        'gui.luminabox.settings.proxy_host': '主機地址',
        'gui.luminabox.settings.proxy_port': '端口',
        'gui.luminabox.settings.save_proxy': '儲存',
        'gui.luminabox.settings.proxy_saved': '代理設定已儲存！重啟遊戲生效。'
    },
    'ja_jp': {
        'gui.luminabox.settings.proxy_title': 'HTTPプロキシ (再起動が必要):',
        'gui.luminabox.settings.proxy_host': 'ホスト',
        'gui.luminabox.settings.proxy_port': 'ポート',
        'gui.luminabox.settings.save_proxy': '保存',
        'gui.luminabox.settings.proxy_saved': '保存しました！再起動後に適用されます。'
    },
    'ko_kr': {
        'gui.luminabox.settings.proxy_title': 'HTTP 프록시 (재시작 필요):',
        'gui.luminabox.settings.proxy_host': '호스트',
        'gui.luminabox.settings.proxy_port': '포트',
        'gui.luminabox.settings.save_proxy': '저장',
        'gui.luminabox.settings.proxy_saved': '저장되었습니다! 재시작 후 적용됩니다.'
    },
    'fr_fr': {
        'gui.luminabox.settings.proxy_title': 'Proxy HTTP (Redémarrage requis):',
        'gui.luminabox.settings.proxy_host': 'Hôte',
        'gui.luminabox.settings.proxy_port': 'Port',
        'gui.luminabox.settings.save_proxy': 'Sauvegarder',
        'gui.luminabox.settings.proxy_saved': 'Sauvegardé ! Redémarrez le jeu.'
    },
    'es_es': {
        'gui.luminabox.settings.proxy_title': 'Proxy HTTP (Requiere reinicio):',
        'gui.luminabox.settings.proxy_host': 'Host',
        'gui.luminabox.settings.proxy_port': 'Puerto',
        'gui.luminabox.settings.save_proxy': 'Guardar',
        'gui.luminabox.settings.proxy_saved': '¡Guardado! Reinicia el juego.'
    },
    'ar_sa': {
        'gui.luminabox.settings.proxy_title': 'HTTP Proxy (يتطلب إعادة تشغيل):',
        'gui.luminabox.settings.proxy_host': 'المضيف',
        'gui.luminabox.settings.proxy_port': 'المنفذ',
        'gui.luminabox.settings.save_proxy': 'حفظ',
        'gui.luminabox.settings.proxy_saved': 'تم الحفظ! أعد تشغيل اللعبة.'
    },
    'pt_br': {
        'gui.luminabox.settings.proxy_title': 'Proxy HTTP (Requer reinício):',
        'gui.luminabox.settings.proxy_host': 'Host',
        'gui.luminabox.settings.proxy_port': 'Porta',
        'gui.luminabox.settings.save_proxy': 'Salvar',
        'gui.luminabox.settings.proxy_saved': 'Salvo! Reinicie o jogo.'
    }
}

for file in files:
    basename = os.path.basename(file).split('.')[0]
    
    with open(file, 'r', encoding='utf-8') as f:
        data = json.load(f)
        
    trans = translations.get(basename, translations['en_us'])
    
    for k, v in trans.items():
        data[k] = v
        
    with open(file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

print('Updated translations.')
