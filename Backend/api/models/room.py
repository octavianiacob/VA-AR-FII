# -*- coding: utf-8 -*-

from models import Waypoint
from neomodel import IntegerProperty, RelationshipFrom, One
from webargs import fields
from models import Floor


class Room(Waypoint):
    """Room model"""
    __validation_rules__ = {
        "floorLevel": fields.Int(required=True),
        **Waypoint.__validation_rules__
    }

    markerId = IntegerProperty(required=True, index=True)
    floorLevel = IntegerProperty(required=True)
    floor = RelationshipFrom('models.Floor', 'HAS', cardinality=One)

    def pre_save(self):
        Floor.nodes.get(buildingName=self.buildingName, level=self.floorLevel)
        self.building_unique_waypoint = f'building_{self.buildingName}_waypoint_name_{self.name}_marker_id_{self.markerId}'

    def post_save(self):
        Floor.nodes.get(buildingName=self.buildingName,
                        level=self.floorLevel).waypoints.connect(self)