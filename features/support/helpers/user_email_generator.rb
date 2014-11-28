module KnowsAboutEmailGeneration
  def random_email
    chars = [*("A".."Z"), *("a".."z"), *("0".."9")]
    "#{chars.sample(40).join}@blinkbox.com"
  end
end

World(KnowsAboutEmailGeneration)
