function test() {
    new Ajax.Updater('output', '/status/scAjax.page', {
        method: 'get',
        parameters: { root: $F('root_url') },
        insertion: Insertion.Top
    });
}