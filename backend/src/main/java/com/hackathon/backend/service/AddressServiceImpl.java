package com.hackathon.backend.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.hackathon.backend.dto.AddressDTO;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Address;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.AddressRepository;
import com.hackathon.backend.security.AuthenticatedUserService;

@Service
public class AddressServiceImpl {

    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            ModelMapper modelMapper,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.addressRepository = addressRepository;
        this.modelMapper = modelMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = authenticatedUserService.getCurrentUser();

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        user.getAddresses().add(address);

        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    public List<AddressDTO> getAddresses() {
        return addressRepository.findAll()
                .stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    public AddressDTO getAddressById(Long addressId) {
        Address address = getAddressByIdOrThrow(addressId);
        validateAddressOwnership(address);
        return modelMapper.map(address, AddressDTO.class);
    }

    public List<AddressDTO> getUserAddresses() {
        Long userId = authenticatedUserService.getCurrentUser().getUserId();
        return addressRepository.findByUserUserId(userId)
                .stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDatabase = getAddressByIdOrThrow(addressId);
        validateAddressOwnership(addressFromDatabase);

        addressFromDatabase.setCity(addressDTO.getCity());
        addressFromDatabase.setPincode(addressDTO.getPincode());
        addressFromDatabase.setState(addressDTO.getState());
        addressFromDatabase.setCountry(addressDTO.getCountry());
        addressFromDatabase.setStreet(addressDTO.getStreet());
        addressFromDatabase.setBuildingName(addressDTO.getBuildingName());
        Address updatedAddress = addressRepository.save(addressFromDatabase);

        User user = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    public String deleteAddress(Long addressId) {
        Address addressFromDatabase = getAddressByIdOrThrow(addressId);
        validateAddressOwnership(addressFromDatabase);

        User user = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        addressRepository.delete(addressFromDatabase);

        return "Address deleted successfully with addressId: " + addressId;
    }

    private Address getAddressByIdOrThrow(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
    }

    private void validateAddressOwnership(Address address) {
        User authenticatedUser = authenticatedUserService.getCurrentUser();
        if (!address.getUser().getUserId().equals(authenticatedUser.getUserId())) {
            throw new AccessDeniedException("You can only access your own addresses");
        }
    }
}
