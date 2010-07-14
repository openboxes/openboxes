package org.pih.warehouse

import grails.converters.JSON


class CountryController {
	def scaffold = Country
	def index = { redirect(action: list, params: params)
	}
	
	def list = {
	}
	
	def listData = {
		def countryList = Country.list(params)
		render([totalRecords:countryList.size(),results:countryList] as JSON)
	}
	
	def delete = {
		def country = Country.get( params.id )
		country.delete()
		render "Country '${country.country}' was deleted"
	}
	
	def anotherWayToReturnJson = {
		params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
		//[ courseInstanceList: Course.list( params ) , courseInstanceTotal: Course.count() ]
		def countries = Country.list( params )
		// return a bunch of json data with metadata.
		def json = [
		totalCount: countries.size,
		results: countries
		]
		render json as JSON
	}
}
