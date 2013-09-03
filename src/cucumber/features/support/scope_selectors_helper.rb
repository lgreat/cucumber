PAGE_SELECTORS ||= {}
def define_page_selectors(page_name, hash)
  PAGE_SELECTORS.merge!({
      page_name => hash
  })
end