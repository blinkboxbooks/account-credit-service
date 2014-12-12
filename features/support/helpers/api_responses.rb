module KnowsAboutAPIResponses
  def parse_last_api_response
    begin
      JSON.load(HttpCapture::RESPONSES.last.body)
    rescue JSON::ParserError
      HttpCapture::RESPONSES.last.body
    end
  end

  def last_response_as_string
    HttpCapture::RESPONSES.last.body.to_s
  end
end
World(KnowsAboutAPIResponses)
