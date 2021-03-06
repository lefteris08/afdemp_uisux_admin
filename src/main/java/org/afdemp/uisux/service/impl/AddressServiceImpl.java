package org.afdemp.uisux.service.impl;

import java.util.List;

import org.afdemp.uisux.domain.Address;
import org.afdemp.uisux.domain.CreditCard;
import org.afdemp.uisux.domain.security.UserRole;
import org.afdemp.uisux.repository.AddressRepository;
import org.afdemp.uisux.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService{
	
	private static final Logger LOG = LoggerFactory.getLogger(AddressService.class);
	
	@Autowired
	private AddressRepository addressRepository;
	
	public Address createAddress(Address address)
	{
		if (address.getUserRole()==null || address.getReceiverName()==null || address.getStreet1()==null || address.getCity()==null || address.getZipcode()==null)
		{
			LOG.info("\n\n\nFAILURE: Insufficient information passed. Unable to create a valid address.\n\n");
			return null;
		}
		else
		{
			Address tempAddress=addressRepository.findByReceiverNameAndStreet1AndCityAndZipcodeAndUserRole(address.getReceiverName(), address.getStreet1(), address.getCity(), address.getZipcode(),address.getUserRole());
			if(tempAddress==null) 
			{
				tempAddress = new Address();
				tempAddress.setReceiverName(address.getReceiverName());
				tempAddress.setStreet1(address.getStreet1());
				tempAddress.setStreet2(address.getStreet2());
				tempAddress.setCity(address.getCity());
				tempAddress.setState(address.getState());
				tempAddress.setCountry(address.getCountry());
				tempAddress.setZipcode(address.getZipcode());
				tempAddress=addressRepository.save(tempAddress);
				LOG.info("SUCCESS: Address Succesfully Added!!");
			}
			
			return tempAddress;
			
			
		}
		
	}

	@Override
	public Address findById(Long shippingAddressId) {
		return addressRepository.findOne(shippingAddressId);
	}

	@Override
	public void setDefaultShippingAddress(Long defaultShippingAddressId, UserRole userRole) {
		List<Address> shippingAddressList = userRole.getUserShippingAddressList();
		
		for (Address sa : shippingAddressList) {
			if (sa.isUserShippingDefault()) {
				sa.setUserShippingDefault(false);
				addressRepository.save(sa);
			}
			if (sa.getId() == defaultShippingAddressId) {
				sa.setUserShippingDefault(true);
				addressRepository.save(sa);
			}
		}
		
	}

}
